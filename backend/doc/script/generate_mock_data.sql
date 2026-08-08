-- ============================================================================
-- 模拟数据生成器 (用户 / 评论 / 新闻 + 统计自洽更新)
-- 用法: mysql --default-character-set=utf8mb4 -uroot -p123456 novel < generate_mock_data.sql
-- 说明:
--   1. 幂等:已有用户/评论/新闻时跳过对应生成,不会重复插入
--   2. 生成用户密码均为 123456 (MD5),可直接登录演示
--   3. 评论/新闻为模板组合生成的模拟内容,仅用于本地演示
-- ============================================================================

DROP PROCEDURE IF EXISTS generate_mock_data;

DELIMITER $$

CREATE PROCEDURE generate_mock_data()
BEGIN
    -- MySQL 8 递归 CTE 深度上限放开(生成 3000 条评论用)
    SET SESSION cte_max_recursion_depth = 10000;

    -- ------------------------------------------------------------------
    -- 1. 生成用户 (仅当 user_info 为空时)
    -- ------------------------------------------------------------------
    IF (SELECT COUNT(*) FROM user_info) = 0 THEN
        DROP TEMPORARY TABLE IF EXISTS tmp_nick_a;
        CREATE TEMPORARY TABLE tmp_nick_a (v VARCHAR(20) PRIMARY KEY);
        INSERT INTO tmp_nick_a VALUES
        ('夜雨'), ('追风'), ('爱吃'), ('安静'), ('暴躁'), ('一只'), ('书荒'),
        ('熬夜'), ('咸鱼'), ('迷路'), ('会飞的'), ('看书的'), ('码字的'),
        ('干饭的'), ('躺平的'), ('修仙的'), ('摸鱼的'), ('秃头的'),
        ('快乐的'), ('神秘的'), ('奔跑的'), ('发呆的'), ('起晚的'), ('加班的');

        DROP TEMPORARY TABLE IF EXISTS tmp_nick_b;
        CREATE TEMPORARY TABLE tmp_nick_b (v VARCHAR(20) PRIMARY KEY);
        INSERT INTO tmp_nick_b VALUES
        ('小猫'), ('柠檬'), ('汤圆'), ('章鱼'), ('白桃'), ('青柠'), ('松饼'),
        ('奶茶'), ('咸鱼'), ('布丁'), ('小鱼'), ('月亮'), ('星星'), ('云朵'),
        ('桃桃'), ('糯米'), ('核桃'), ('蘑菇'), ('柚子'), ('山茶'), ('橘子'),
        ('栗子'), ('榴莲'), ('西瓜'), ('菠萝');

        INSERT INTO user_info (username, password, salt, nick_name, user_sex,
                               account_balance, status, create_time, update_time)
        WITH RECURSIVE seq AS (SELECT 1 n UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
        SELECT
            CONCAT('1380000', LPAD(n, 4, '0')),                       -- 手机号唯一
            'e10adc3949ba59abbe56e057f20f883e',                       -- md5('123456')
            '0',
            CONCAT((SELECT v FROM tmp_nick_a ORDER BY RAND() LIMIT 1),
                   (SELECT v FROM tmp_nick_b ORDER BY RAND() LIMIT 1)), -- 随机昵称
            FLOOR(RAND() * 3),                                        -- 性别 0未知 1男 2女
            FLOOR(RAND() * 10000) * 100,                              -- 余额 0 ~ 100万
            0,                                                        -- 状态正常
            TIMESTAMPADD(SECOND, -FLOOR(RAND() * 15552000), NOW()),   -- 近 180 天注册
            NOW()
        FROM seq;

        DROP TEMPORARY TABLE IF EXISTS tmp_nick_a;
        DROP TEMPORARY TABLE IF EXISTS tmp_nick_b;
    END IF;

    -- ------------------------------------------------------------------
    -- 2. 生成评论 (仅当 book_comment 为空时)
    -- ------------------------------------------------------------------
    IF (SELECT COUNT(*) FROM book_comment) = 0 THEN
        DROP TEMPORARY TABLE IF EXISTS tmp_comment_tpl;
        CREATE TEMPORARY TABLE tmp_comment_tpl (v VARCHAR(300) PRIMARY KEY);
        INSERT INTO tmp_comment_tpl VALUES
        ('一口气看到最新章节，根本停不下来，坐等更新！'),
        ('这本书的设定很有意思，节奏也快，值得追。'),
        ('文笔流畅，人物塑造立体，配角都很有记忆点。'),
        ('前期铺垫稍慢，中后期渐入佳境，坚持看下去有惊喜。'),
        ('追更半年了，质量一直在线，作者大大辛苦了！'),
        ('看到一半回来评论，这剧情反转我是真没想到。'),
        ('室友推荐来看的，确实不错，已加入书架。'),
        ('说实话开头差点劝退，坚持到第三章真香了。'),
        ('金手指开得合理，不无脑爽，细节经得起推敲。'),
        ('每天追更的快乐你们不懂，评论区蹲一个同好。'),
        ('这本书让我重新爱上了看小说，强烈推荐！'),
        ('作者更新时间很稳定，体验极佳。'),
        ('有点套路化，但就是好看，爽点密集。'),
        ('世界观架构很完整，设定党狂喜。'),
        ('配角智商在线，不降智，好评。'),
        ('看到最新章了，剧情卡在高潮处，催更！'),
        ('整体不错，就是某些情节有点拖，希望后面能紧凑些。'),
        ('第一次评论，这本书值得五星。'),
        ('书荒很久了，这本书救了我，已收藏。'),
        ('节奏明快不注水，作者是懂读者的。'),
        ('修炼体系设定很新颖，升级节奏舒服，一口气看了三百章。'),
        ('职场描写真实，主角成长线很燃，打工人代入感拉满。'),
        ('伏笔埋得深，反转一个接一个，看得头皮发麻。'),
        ('考据扎实，权谋线精彩，历史党狂喜。'),
        ('脑洞大开，设定自洽，科幻迷必读。'),
        ('感情线细腻，女主不傻白甜，甜而不腻。'),
        ('这章的斗法场面写得太燃了，反复看了三遍。'),
        ('开局有点平淡，但坚持到上架后剧情起飞。'),
        ('作者太懂读者了，爽点卡得恰到好处。'),
        ('熬夜看完最新章，明天还要上班，难受但快乐。'),
        ('从第一章追到现在，见证了一本神作的诞生。'),
        ('书评区气氛很好，讨论剧情的人很多，氛围不错。'),
        ('文风轻松幽默，适合下班放松的时候看。'),
        ('主角不圣母，做事有原则，看着舒服。'),
        ('有些坑到现在还没填，作者你记得吗？'),
        ('群像写得好，每个人物都有血有肉，不是工具人。'),
        ('这本比同类型的大多数书都好看，不懂为什么评分不高。'),
        ('断更一周了，作者快回来！！'),
        ('设定集我都能看一小时，细节控的福音。'),
        ('看完这部准备把作者的其他书也刷一遍。'),
        ('这一章的铺垫终于收线了，前面埋的伏笔全都圆上了。'),
        ('主角的成长轨迹很清晰，一步一步靠实力走出来。'),
        ('战斗场面有点水字数，不过日常部分写得很好。'),
        ('说实话这书后劲挺大，看完两章不自觉地想继续。'),
        ('为爱发电的作者值得支持，已打赏。'),
        ('剧情进入新地图了，期待后面的发展。'),
        ('这本书让我戒掉了短视频，全靠它了。'),
        ('文笔在网文里算上层，描写有画面感。'),
        ('感情线进展有点慢，急死我了，快告白啊！'),
        ('看完这章泪目了，这段写得真好。');

        -- 用户 x 书的笛卡尔积 (109 x 100) 随机取 3000 行, 保证 (book_id, user_id) 唯一
        INSERT INTO book_comment (book_id, user_id, comment_content, reply_count,
                                  audit_status, create_time, update_time)
        SELECT
            b.id,                                                  -- 随机书
            u.id,                                                  -- 随机用户
            (SELECT v FROM tmp_comment_tpl ORDER BY RAND() LIMIT 1), -- 随机模板
            FLOOR(RAND() * 5),                                     -- 回复数
            0,                                                     -- 审核通过
            TIMESTAMPADD(SECOND, -FLOOR(RAND() * 15552000), NOW()), -- 近 180 天
            NOW()
        FROM book_info b
        CROSS JOIN user_info u
        ORDER BY RAND()
        LIMIT 3000;

        DROP TEMPORARY TABLE IF EXISTS tmp_comment_tpl;
    END IF;

    -- ------------------------------------------------------------------
    -- 3. 生成新闻 (仅当 news_info 少于 5 条时补充标题, 正文缺失时幂等补全)
    -- ------------------------------------------------------------------
    DROP TEMPORARY TABLE IF EXISTS tmp_news_title;
    CREATE TEMPORARY TABLE tmp_news_title (v VARCHAR(150) PRIMARY KEY);
    INSERT INTO tmp_news_title VALUES
    ('《{book}》上架一周，点击突破百万'),
    ('本站签约作家 {author} 新书《{book}》火热连载中'),
    ('本周热门榜出炉，《{book}》强势登顶'),
    ('编辑推荐：《{book}》——{author} 又一力作'),
    ('平台作家扶植计划启动，稿酬最高翻倍'),
    ('月度书评活动开始，参与赢取会员'),
    ('《{book}》完结撒花，全本订阅限时优惠'),
    ('读者投票选出的年度期待新书：《{book}》'),
    ('AI 写作助手正式上线，助力作家创作'),
    ('本周更新榜：{author} 连更 7 天，劳模预定'),
    ('专访 {author}：创作《{book}》背后的故事'),
    ('秋季征文大赛开启，万元大奖等你来'),
    ('《{book}》实体出版签约，改编进行中'),
    ('平台会员日福利：全场 5 折起'),
    ('新人作家扶持计划，首月稿酬双倍');

    DROP TEMPORARY TABLE IF EXISTS tmp_news_para;
    CREATE TEMPORARY TABLE tmp_news_para (v VARCHAR(500) PRIMARY KEY);
    INSERT INTO tmp_news_para VALUES
    ('本站讯，近日平台多部作品表现亮眼，读者阅读热情持续高涨，热门榜单竞争激烈。编辑部将持续为大家带来优质内容推荐。'),
    ('为了鼓励更多优秀创作者，平台正式启动作家扶植计划，从签约、推广到稿酬结算提供一站式支持，助力每一位有梦想的作者。'),
    ('编辑部收到大量读者来信反馈，表示平台推荐算法精准，书单质量高，书荒问题得到明显缓解。'),
    ('本月会员日活动将于月底开启，全场订阅限时折扣，敬请期待。同时新注册用户可领取新人阅读礼包。'),
    ('平台技术团队持续优化阅读体验，新版阅读器支持自定义字体、夜间模式与跨设备进度同步。'),
    ('多位签约作家表示，平台的数据反馈清晰透明，有助于他们更好地了解读者喜好，调整创作方向。'),
    ('根据平台数据中心统计，本周玄幻类作品阅读量环比增长 32%，悬疑推理类作品增长最快。'),
    ('平台版权运营团队已与多家出版机构达成合作，多部优质作品将陆续推出实体书与有声书版本。'),
    ('为保障读者阅读体验，平台将加强内容审核机制，营造健康积极的创作与阅读环境。'),
    ('本月新增注册读者数量再创新高，平台运营团队将持续优化推荐策略，让好书遇见对的人。');

    IF (SELECT COUNT(*) FROM news_info) < 5 THEN
        INSERT INTO news_info (category_id, category_name, source_name, title,
                               create_time, update_time)
        WITH RECURSIVE seq AS (SELECT 1 n UNION ALL SELECT n + 1 FROM seq WHERE n < 15)
        SELECT
            FLOOR(1 + RAND() * 5),
            (SELECT name FROM news_category ORDER BY RAND() LIMIT 1),
            '本站编辑',
            REPLACE(REPLACE((SELECT v FROM tmp_news_title ORDER BY RAND() LIMIT 1),
                            '{book}', (SELECT book_name FROM book_info ORDER BY RAND() LIMIT 1)),
                    '{author}', (SELECT author_name FROM book_info ORDER BY RAND() LIMIT 1)),
            TIMESTAMPADD(SECOND, -FLOOR(RAND() * 15552000), NOW()),
            NOW()
        FROM seq;
    END IF;

    -- 新闻正文 (临时表副本, 避免同一语句多次引用临时表; 缺正文的新闻幂等补全)
    DROP TEMPORARY TABLE IF EXISTS tmp_news_para2;
    CREATE TEMPORARY TABLE tmp_news_para2 AS SELECT * FROM tmp_news_para;
    DROP TEMPORARY TABLE IF EXISTS tmp_news_para3;
    CREATE TEMPORARY TABLE tmp_news_para3 AS SELECT * FROM tmp_news_para;

    INSERT INTO news_content (news_id, content, create_time, update_time)
    SELECT
        n.id,
        CONCAT((SELECT v FROM tmp_news_para  ORDER BY RAND() LIMIT 1), '\n\n',
               (SELECT v FROM tmp_news_para2 ORDER BY RAND() LIMIT 1), '\n\n',
               (SELECT v FROM tmp_news_para3 ORDER BY RAND() LIMIT 1)),
        n.create_time,
        NOW()
    FROM news_info n
    WHERE NOT EXISTS (SELECT 1 FROM news_content c WHERE c.news_id = n.id);

    DROP TEMPORARY TABLE IF EXISTS tmp_news_title;
    DROP TEMPORARY TABLE IF EXISTS tmp_news_para;
    DROP TEMPORARY TABLE IF EXISTS tmp_news_para2;
    DROP TEMPORARY TABLE IF EXISTS tmp_news_para3;

    -- ------------------------------------------------------------------
    -- 4. 统计字段自洽更新
    -- ------------------------------------------------------------------
    UPDATE book_info b
    SET b.comment_count = (SELECT COUNT(*) FROM book_comment c WHERE c.book_id = b.id);

    UPDATE book_info
    SET visit_count = visit_count + FLOOR(RAND() * 30000) + 5000;

    SELECT 'generate done'
        , (SELECT COUNT(*) FROM user_info)    AS user_total
        , (SELECT COUNT(*) FROM book_comment) AS comment_total
        , (SELECT COUNT(*) FROM news_info)    AS news_total;
END$$

DELIMITER ;

CALL generate_mock_data();
