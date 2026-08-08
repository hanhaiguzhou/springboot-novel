# 开发指南

> 面向学习者的架构解析与上手指南，读完能理解项目怎么分层、数据怎么流转、怎么自己加功能。

## 目录

- [一、项目全景](#一项目全景)
- [二、分层架构](#二分-层-架-构)
- [三、请求处理全链路](#三请求处理全链路)
- [四、缓存体系](#四缓存体系)
- [五、认证授权](#五认证授权)
- [六、AI 功能实现](#六ai-功能实现)
- [七、如何新增一个功能](#七如何新增一个功能)
- [八、数据库表结构概览](#八数据库表结构概览)
- [九、前端组件地图](#九前端组件地图)
- [十、常见问题](#十常见问题)

---

## 一、项目全景

```
┌─────────────────────────────────────────────────┐
│  前端 (Vue 3)  :1024                             │
│  Home.vue  Book.vue  BookContent.vue ...        │
│  axios → /api/front/*  /api/author/*            │
└──────────────────┬──────────────────────────────┘
                   │ HTTP JSON
┌──────────────────▼──────────────────────────────┐
│  后端 (Spring Boot 3)  :8888                     │
│                                                  │
│  Filter(XssFilter) → Interceptor(Auth)          │
│       → Controller → Service → Manager → Dao    │
│                                                  │
│  Caffeine(本地缓存) + Redis(分布式缓存)            │
│  Spring AI → SiliconFlow(DeepSeek)               │
│  MySQL 8.0                                       │
└─────────────────────────────────────────────────┘
```

**技术看点:**
- 后端分层: `controller → service → manager → dao`(阿里规范,比三层架构多一层 manager 做通用能力下沉)
- 缓存: **Caffeine 本地缓存** + **Redis 远程缓存** 双缓存架构,按数据类型选择不同策略
- 认证: 基于请求前缀 `front/author/admin` 动态选择认证策略(策略模式)
- AI: Spring AI ChatClient + 结构化 JSON 输出 + Caffeine 限流
- JDK 21 虚拟线程: `spring.threads.virtual.enabled=true`

---

## 二、分层架构

```java
io.github.xxyopen.novel
├── controller          // 接入层:接收 HTTP 请求，参数校验，调用 service，返回 JSON
│   ├── front/          // 门户接口(首页、书籍、用户、搜索、AI ...)
│   ├── author/         // 作家后台接口
│   └── admin/          // 平台后台接口(预留)
│
├── service             // 业务层:具体的业务逻辑编排
│   └── impl/
│
├── manager             // 通用处理层:对 service 层通用能力 + 第三方平台封装 + 多 DAO 组合
│   ├── cache/          // 缓存管理(BookInfoCacheManager 等)
│   ├── message/        // 消息发送(邮件、系统通知)
│   ├── redis/          // Redis 封装(验证码)
│   ├── mq/             // 消息队列
│   └── dao/            // 多 DAO 组合复用
│
├── dao                 // 数据访问层:MyBatis-Plus Mapper
│   ├── entity/         // 数据库实体
│   └── mapper/         // MyBatis-Plus 接口
│
├── dto                 // 数据传输对象
│   ├── req/            // 请求 DTO
│   └── resp/           // 响应 DTO
│
└── core                // 核心支撑
    ├── common/         // 通用组件(RestResp、PageReqDto、异常处理...)
    ├── config/         // Spring 配置类
    ├── constant/       // 常量
    ├── interceptor/    // 拦截器(认证/限流/文件)
    ├── auth/           // 认证策略
    └── filter/         // 过滤器(XSS)
```

**为什么多一层 manager?**
- `service` 关注业务流程编排，尽量轻量
- `manager` 把可复用的"原子能力"下沉:缓存读写、第三方 API 调用、多个 DAO 联合查询
- 典型调用:`BookServiceImpl.getBookById()` → `BookInfoCacheManager.getBookInfo()` → `BookInfoMapper.selectById()`

---

## 三、请求处理全链路

以「点击书籍详情页」为例，追踪一次完整请求:

```
用户点击 /book/123
  │
  ▼
前端 Book.vue.onMounted()
  → getBookById(123)
  → GET /api/front/book/123
  │
  ▼
后端 Filter 链
  → XssFilter(防 XSS 注入)
  → AuthInterceptor.preHandle()
    → 从请求头取 JWT token
    → 解析 URI: /api/front/book/123 → systemName = "front"
    → 找到 FrontAuthStrategy.auth()
    → 把 token 中的 userId 放入 UserHolder(ThreadLocal)
  │
  ▼
BookController.getBookById(123)
  → bookService.getBookById(123)
  │
  ▼
BookServiceImpl.getBookById(123)
  → bookInfoCacheManager.getBookInfo(123)
    → @Cacheable → 检查 Caffeine 本地缓存
      → 命中:直接返回 ✅
      → 未命中:
        → bookInfoMapper.selectById(123)  // MyBatis-Plus 查 MySQL
        → 未找到 → throw BusinessException(BOOK_NOT_FOUND)
        → 找到 → 同时查首章 ID(BookChapterMapper)
        → 组装 BookInfoRespDto → 放入 Caffeine 缓存 → 返回
  │
  ▼
返回 RestResp<BookInfoRespDto>  →  Spring 自动序列化 JSON
  │
  ▼
AuthInterceptor.afterCompletion()
  → UserHolder.clear()  // 清理 ThreadLocal，防止内存泄漏
  │
  ▼
前端收到 data → state.book = data → 渲染页面
```

**关键点:**
- `UserHolder` 用 `ThreadLocal` 存当前用户,请求结束必须 clear,否则在线程池复用场景下会串数据
- Controller 只做薄薄一层:拿参数调 service,包装 RestResp 返回
- Service 也不干活——它主要做编排,把活派给 Manager
- **所有数据库查询都经过 CacheManager**,不会直接在 Service 里写 SQL

---

## 四、缓存体系

项目用 **Caffeine(本地) + Redis(远程)** 双缓存,按数据类型分级:

```
┌──────────────────────────────────────────┐
│  CacheConfig                              │
│                                           │
│  caffeineCacheManager (本地 JVM 内存)      │
│  ├─ bookInfoCache      TTL 18h            │
│  ├─ bookChapterCache   TTL 6h             │
│  ├─ bookCategoryCache  TTL ∞              │
│  ├─ homeBookCache      TTL 24h            │
│  └─ ...                                  │
│                                           │
│  redisCacheManager (远程 Redis)            │
│  ├─ bookContentCache   TTL 12h(内容大)     │
│  ├─ bookVisitRankCache TTL 6h             │
│  ├─ userInfoCache      TTL 24h            │
│  └─ ...                                  │
└──────────────────────────────────────────┘
```

**缓存分级策略(CacheEnum.type):**
| type | 含义 | 适用场景 |
|------|------|----------|
| 0 | 仅本地 Caffeine | 分类列表、排行榜(高频读、小数据量、允许不一致) |
| 1 | 本地 + 远程 | (本项目暂少用) |
| 2 | 仅远程 Redis | 章节内容(大数据量)、点击榜(多实例共享)、用户信息(需跨实例) |

**为什么小说内容用 Redis 不用 Caffeine?**
- 一章正文可能几 KB~几十 KB,全放在本地内存会撑爆 JVM
- Redis 集中管理,多实例共享一份

**缓存更新策略:**
- 读: `@Cacheable` 自动检查缓存,未命中则执行方法体
- 写: `@CachePut` 更新数据时同步刷新缓存
- 删: `@CacheEvict` 数据变更时清掉缓存(下次读会重新加载)

---

## 五、认证授权

**无侵入式认证——策略模式:**

```
请求 URL: /api/front/book/123
  │
  ▼
AuthInterceptor.preHandle()
  │
  ├─ 解析 URI → systemName = "front"
  ├─ 拼接策略名 = "frontAuthStrategy"
  ├─ 从 Spring 容器取出 FrontAuthStrategy Bean
  └─ auth(token, requestUri)
       │
       ├─ 不需要登录的接口(首页/榜单/书详情) → 直接放行
       ├─ 需要登录的接口(评论/用户中心) → 验证 JWT → 解析 userId → UserHolder.set()
       └─ 鉴权失败 → 返回 "用户登录已过期" 或 "访问未授权"
```

**三种策略:**
| 策略类 | 对应前缀 | 特点 |
|--------|---------|------|
| `FrontAuthStrategy` | `/api/front` | 门户接口:部分需要登录 |
| `AuthorAuthStrategy` | `/api/author` | 作家后台:全部需要登录+作家身份 |
| `AdminAuthStrategy` | `/api/admin` | 平台后台:管理员权限(预留) |

**JWT 流程:**
1. 登录成功 → 后端用 `JwtUtils.generateToken(userId)` 生成 token
2. 前端存到 localStorage,每次请求通过 `request.js` 拦截器自动带在 `Authorization` 头
3. 后端拦截器解析 token → 取 userId → 放 ThreadLocal
4. 请求结束清理 ThreadLocal

---

## 六、AI 功能实现

### 6.1 配置

```yaml
spring:
  ai:
    openai:
      api-key: sk-xxx                    # SiliconFlow 的 API Key
      base-url: https://api.siliconflow.cn   # 国内可直连的模型托管平台
      chat:
        options:
          model: deepseek-ai/DeepSeek-R1-0528-Qwen3-8B  # 推理增强模型
```

用 OpenAI 兼容协议,把 `base-url` 改成 SiliconFlow 就自动走国内代理(成本极低,约 ¥0.1/万 token)。

### 6.2 作家写作助手(已有)

`AuthorAiController` → 4 个接口:

```
POST /api/author/ai/expand    → 扩写(填字数)
POST /api/author/ai/condense  → 缩写(精简)
POST /api/author/ai/continue  → 续写(往下写)
POST /api/author/ai/polish    → 润色(优化表达)
```

前端 `ChapterAdd.vue`:选中文本 → 点击 AI 按钮 → 弹出参数设置 → 调用接口 → 打字效果追加到编辑器。

### 6.3 读者 AI 评价(本次新增)

`FrontAiController` → `FrontAiServiceImpl` → 4 个接口,这是**最值得学习**的部分:

#### AI 书评 → `POST /api/front/ai/book_review`

```java
// 核心流程:
BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
String prompt = "书名：" + bookInfo.getBookName() + " 简介：" + bookInfo.getBookDesc();
String content = chatClient.prompt()
    .system("你是资深书评人,请以 JSON 格式输出...")  // 系统指令
    .user(prompt)                                    // 用户输入
    .call()
    .content();                                       // 获取 AI 回复
// 解析 JSON → 结构化 AiBookReviewRespDto(评分/亮点/槽点/适合人群)
// 解析失败则降级为 rawText 原文展示
```

**设计要点:**
1. **System Prompt + User Prompt 分离**:system 定义身份和输出格式,user 只给内容,调参时改 system 即可
2. **JSON 解析降级**:R1 推理模型可能不严格输出纯 JSON(会带 `{...}`),用 `extractJson()` 取第一个 `{` 到最后一个 `}` 的内容再解析;解析失败则展示原始文本,用户依然能看到 AI 的输出
3. **Prompt 工程示例**:
   ```
   System: "你是资深网络小说书评人,善于从读者视角给出客观、有趣、真诚的短书评。
            严格以 JSON 格式输出:{\"score\": 8, \"summary\": \"...\", \"pros\": [...], \"cons\": [...], \"audience\": \"...\"}"

   User: "书名：《斗破苍穹》 作者：天蚕土豆 分类：玄幻 简介：天才少年萧炎..."
   ```

#### AI 章节导读 → `POST /api/front/ai/chapter_summary`

- 取章节名 + 正文前 1500 字(截断避免 token 超限)
- prompt 要求输出"剧情概述 + 伏笔提示 + 阅读体验",200 字内
- 返回纯文本,前端弹窗展示

#### 评论草稿 → `POST /api/front/ai/comment_draft`

- 取书籍信息 + 用户已写的草稿内容
- prompt: "补全并润色成一条 50-120 字的读者书评,口语化有梗,不要出现'作为AI'"
- 返回后自动填入评论框,用户可修改后再发表

#### AI 智能荐书 → `GET /api/front/ai/recommend`

- 从点击榜+新书榜取候选(top 15)
- prompt: "从候选书中挑 5 本,每本一句 15-30 字推荐语"
- **关键**:AI 返回 `[{bookId, reason}]`,后端用 `bookId` 去候选池里匹配,补充封面、作者信息
- **降级策略**:AI 解析失败 → 直接用点击榜前 5 本 + 固定文案"热门人气作品"

### 6.4 限流设计

`AiRateLimiter` — 基于 Caffeine 的简易滑动窗口限流:

```java
// 1 分钟窗口内,同一 bookId 最多调用书评接口 3 次
if (aiRateLimiter.isLimited("bookReview:" + bookId, 3)) {
    return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
}
```

**为什么不用 Redis 限流?**
- AI 调用成本直接和 API Key 挂钩,单机限流即可防滥刷
- Caffeine 本地内存的 minute 级窗口足够可靠,复杂度远低于 Redis + Lua

### 6.5 前端 AI 交互模式

| 页面 | AI 入口 | 交互方式 |
|------|---------|----------|
| `Book.vue` 详情 | 「AI评价」按钮 | 弹窗展示结构化书评(评分/亮点/槽点) |
| `Book.vue` 评论区 | 「AI帮写」按钮 | 生成草稿填入评论框,用户可改 |
| `BookContent.vue` 阅读 | 「AI 导读」按钮 | 弹窗展示本章要点+伏笔+体验 |
| `Home.vue` 首页 | 「AI 为你荐书」模块 | 5 本书的封面卡片,含 AI 推荐语 |

---

## 七、如何新增一个功能

以「新增一个"AI 角色百科"功能,输入角色名搜索书中人物关系」为例:

### 步骤 1:加后端接口

```
1. dto/resp/AiCharacterRespDto.java     ← 返回数据结构
2. service/FrontAiService.java          ← 接口定义加方法
   → characterEncyclopedia(Long bookId, String characterName)
3. service/impl/FrontAiServiceImpl.java ← 实现
   ① 取书信息(bookInfoCacheManager)
   ② 构造 prompt:"请搜索《书名》中关于角色 XX 的信息:性格/能力/关系网"
   ③ chatClient.prompt().call().content()
   ④ 解析返回
4. controller/front/FrontAiController.java ← 加端点
   → @PostMapping("character_encyclopedia")
```

### 步骤 2:加前端 API 和页面

```
5. frontend/src/api/ai.js               ← 加 axios 封装
6. 选挂载页面(如 Book.vue 加"角色百科"按钮) ← 仿照 openAiReview 写
```

### 加一个非 AI 的功能更简单(不需要 AI 调参):

```
1. dao/entity/   ← 加实体
2. dao/mapper/   ← 加 MyBatis-Plus Mapper
3. manager/cache/ ← 写缓存管理(如果不加缓存,直接在 service 调 mapper 也行)
4. service/      ← 业务逻辑
5. controller/   ← 暴露接口
6. 前端 api/ + 页面 ← 对前端接口
```

---

## 八、数据库表结构概览

| 表 | 说明 | 关键字段 |
|----|------|----------|
| `book_info` | 小说信息 | id, book_name, author_name, book_desc, category_id, word_count |
| `book_chapter` | 章节信息 | id, book_id, chapter_name, chapter_num |
| `book_content` | 章节正文 | chapter_id, content |
| `book_category` | 小说分类 | id, name, work_direction(0男/1女) |
| `book_comment` | 作品评论 | book_id, user_id, comment_content |
| `user_info` | 用户信息 | id, username, password, nickname |
| `author_info` | 作家信息 | user_id, pen_name, invite_code |
| `home_book` | 首页推荐 | book_id, type(轮播/强推/热门/精品) |
| `home_friend_link` | 友情链接 | link_name, link_url |
| `news_info` | 新闻资讯 | title, source_name, category_id |
| `sys_user/role/menu` | 后台管理(预留) | RBAC 权限模型 |

数据初始化:`doc/sql/novel_struc.sql`(建库建表) + `novel_data.sql`(种子数据,108MB,不入 git,可联系原作者获取)。

---

## 九、前端组件地图

```
frontend/src/
├── App.vue                    # 根组件
├── main.js                    # 入口,Vue.use(ElementPlus)
│
├── api/                       # 接口层(1 文件 = 1 模块)
│   ├── home.js               # 首页相关
│   ├── book.js               # 小说相关
│   ├── user.js               # 用户相关
│   ├── author.js             # 作家后台(含 aiGenerate)
│   ├── ai.js                 # AI 门户(新增)
│   └── ...
│
├── router/index.js           # 路由表(Vue Router Hash 模式)
│
├── views/                    # 页面组件
│   ├── Home.vue              # 首页(轮播 + 强推 + AI 荐书)
│   ├── Book.vue              # 书详情(简介 + 评论 + AI 评价)
│   ├── BookContent.vue       # 阅读页(正文 + AI 导读)
│   ├── BookClass.vue         # 分类搜索
│   ├── BookRank.vue          # 排行榜
│   ├── Login.vue / Register.vue
│   └── author/               # 作家后台
│       ├── ChapterAdd.vue    # 发布章节(含 AI 扩写/缩写/续写/润色)
│       └── ...
│
├── components/               # 可复用组件
│   ├── common/               # 通用(Header, Footer, Navbar, Top)
│   ├── home/                 # 首页子组件(排行榜卡片等)
│   └── user/                 # 用户子组件
│
├── utils/                    # 工具
│   ├── request.js            # axios 拦截器(自动带 token/统一错误弹窗)
│   └── auth.js               # token 存取(getToken/setToken/removeToken)
│
└── assets/                   # 静态资源
    ├── styles/               # 全局 CSS(book.css, read.css, main.css...)
    └── images/               # 图片
```

**数据流:**
```
页面 onMounted()
  → api/xxx.js (axios)
    → request.js 拦截器加 Authorization
      → 后端 Controller → Service → Manager → Dao
    → request.js 拦截器统一处理错误
  → 页面更新 state → 响应式渲染
```

---

## 十、常见问题

### Q1: 启动后端报 "Redis 连接失败"

项目依赖 Redis 做缓存(默认连接 `localhost:6379`),先确保 Redis 已启动。如果不想装 Redis,可以暂时注掉 `spring-boot-starter-data-redis` 依赖——但缓存功能会用不了。

### Q2: AI 接口返回"请求超出限制"

AI 接口有 Caffeine 限流:书评每书 3 次/分钟,推荐 5 次/分钟。等一分钟或换个 bookId 试试。

### Q3: AI 返回的内容不对/乱码

用的是 DeepSeek R1 推理模型,有时不会严格按 JSON 格式输出。代码里有 `extractJson()` 做兜底,但极端情况可能解析失败——此时会展示原始文本(rawText)。可以尝试调整 prompt 或更换模型(如 `deepseek-ai/DeepSeek-V3`)。

### Q4: 前端请求后端 404

检查 `frontend/.env.development` 里的 `VUE_APP_BASE_API_URL` 是否指向 `http://localhost:8888/api`,后端端口是否 8888。

### Q5: 数据库连不上

`application.yml` 里默认 `spring.datasource.url` 指向 `localhost:3306`,用户名 root 密码 123456。先确认 MySQL 已启动,再执行 `doc/sql/novel_struc.sql` 建库。

### Q6: 分库分表报错

`shardingsphere.enabled=false`,项目默认关闭分库分表(学习用,单库就够了)。

### Q7: JDK 版本要求

需要 JDK 21(虚拟线程特性 + Spring Boot 3.3 最低要求)。Maven 3.8+。

### Q8: 想用其他 AI 模型怎么换?

改 `application.yml` 的 `spring.ai.openai`:
- `base-url`: 换成 OpenAI/智谱/通义千问等任何兼容 OpenAI 协议的 API 地址
- `api-key`: 换成对应的密钥
- `model`: 换成对应的模型名

不需要改任何代码——Spring AI 提供了统一抽象。
