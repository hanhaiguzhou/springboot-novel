# 笔阁 · 新中式国风重构设计规范

> 本文件是「笔阁」前端重构的设计系统定稿（对应 designs/novel-redesign.pen 的 Frame 1 设计系统内容）。
> 设计令牌已全部落地于 `frontend/src/assets/styles/base.css`，可直接对照开发。

## 1. 品牌标识

- **印章式 logo**：朱砂方印（圆角 18%）+ 楷体白文「笔阁」二字竖排，内框宣纸色细线
  - 方形印章：`frontend/src/assets/images/bige-seal.svg` / `bige-seal.png`
  - 横向组合（印章 + 楷体墨字 + BIGE·NOVEL）：`logo.svg` / `logo.png`
  - 深色底版本：`logo-white.svg` / `logo_white.png`
  - favicon：`frontend/public/favicon.ico`（16/32/48 多尺寸朱砂印）

## 2. 色板

| 令牌 | 色值 | 用途 |
| --- | --- | --- |
| `--paper` | #F7F3EC | 宣纸米色 · 页面底色 |
| `--paper-deep` | #F0E9DC | 分区/表头底 |
| `--card` | #FFFDF8 | 卡片白（偏暖） |
| `--cinnabar` | #9E2B25 | 印章朱砂 · 主色（按钮/链接悬停/选中态） |
| `--cinnabar-deep` | #7E211C | 朱砂深 · 悬停 |
| `--cinnabar-soft` | #B85A50 | 朱砂浅（榜单 2 名等） |
| `--cinnabar-fade` | rgba(158,43,37,.08) | 浅朱砂底（选中/hover） |
| `--ink` | #2B2622 | 墨色 · 主文字/导航条 |
| `--ink-2` | #5A534B | 次文字 |
| `--ink-3` | #8A8178 | 弱文字 |
| `--ink-4` | #B5AB9C | 占位/禁用 |
| `--line` | #E4DCCB | 水墨细线 |
| `--line-deep` | #D5CBB6 | 深细线 |
| `--gold` | #B08D57 | 赭金点缀（VIP、收费标） |
| `--bamboo` | #5F6F52 | 竹青辅助 |

Element Plus 2.2 换肤：`--el-color-primary` 及 light-3/5/7/8/9、dark-2 全量映射朱砂系（见 base.css）。

## 3. 字体

- 标题：`--font-serif`（思源宋体 → Noto Serif SC → STSong → SimSun）
- 点缀/空状态文案：`--font-kai`（楷体）
- 正文/UI：`--font-sans`（PingFang / 雅黑）

## 4. 组件规范

- **主导航**：墨色长卷底 + 宣纸色宋体字（字距 2px），当前/悬停朱砂下划线
- **按钮**：朱砂实心（.btn_red/.btn_ora）、朱砂描边白底（.btn_ora_white）、灰宣（.btn_gray），圆角 20px 胶囊
- **卡片**：var(--card) 底 + var(--line) 1px 细边 + 3-4px 圆角 + var(--shadow-soft) 淡阴影
- **分页**：细线方框，当前页朱砂实心方块
- **表单**：输入框聚焦朱砂描边 + 3px 浅朱砂光晕；标签墨色
- **弹窗**：6px 圆角 + var(--shadow-lift)；标题宋体 17px
- **表格**：表头宣纸深底 + 宋体；行 hover 浅朱砂底
- **板块标题**：宋体 + 左侧 4px 朱砂竖条（或 10px 朱砂小方块）
- **榜单徽章**：1/2/3 名朱砂/朱砂浅/浅绛方块
- **空状态**：楷体文案（如「案头尚无书卷，何不执笔开篇？」）

## 5. 20 页 Frame 落地对照

全部页面以 base.css/main.css/book.css/user.css/about.css/read.css 全局类 + 页面 scoped 精修实现，
Vue 文件为唯一代码源。页面清单与重构要点见各 `.vue` 文件（P2 公共骨架 6 件 + P3 前台 12 页 + P4 管理端 6 页）。
