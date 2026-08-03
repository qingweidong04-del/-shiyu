-- ============================================
-- 拍遇存 — 数据库初始化脚本
-- 使用方法：mysql -u root -p < init.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS paiyucun DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE paiyucun;

-- 诗词库
DROP TABLE IF EXISTS t_discovery;
DROP TABLE IF EXISTS t_poem;

CREATE TABLE t_poem (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    title         VARCHAR(100)  NOT NULL COMMENT '诗名',
    author        VARCHAR(50)   NOT NULL COMMENT '作者',
    dynasty       VARCHAR(20)   DEFAULT NULL COMMENT '朝代',
    content       TEXT          NOT NULL COMMENT '完整诗文',
    match_line    VARCHAR(255)  NOT NULL COMMENT 'AI匹配句',
    pinyin        TEXT          DEFAULT NULL COMMENT '匹配句拼音',
    full_pinyin   TEXT          DEFAULT NULL COMMENT '全诗拼音',
    translation   VARCHAR(255)  DEFAULT NULL COMMENT '匹配句翻译',
    explanation   TEXT          DEFAULT NULL COMMENT '全文释义',
    keywords      VARCHAR(500)  DEFAULT NULL COMMENT '关键词逗号分隔',
    object_name   VARCHAR(50)   DEFAULT NULL COMMENT 'AI识别物体',
    scene         VARCHAR(100)  DEFAULT NULL COMMENT '场景',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT       DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诗词库';

-- 发现记录
CREATE TABLE t_discovery (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    image_url     VARCHAR(500)  DEFAULT NULL COMMENT '图片URL',
    object_name   VARCHAR(50)   DEFAULT NULL COMMENT '识别物体',
    scene         VARCHAR(100)  DEFAULT NULL COMMENT '场景',
    confidence    DECIMAL(3,2)  DEFAULT NULL COMMENT '置信度',
    poem_id       BIGINT        DEFAULT NULL COMMENT '关联诗词ID',
    poem_line     VARCHAR(255)  DEFAULT NULL COMMENT '诗句',
    poem_source   VARCHAR(100)  DEFAULT NULL COMMENT '出处',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT       DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发现记录';

-- 预置诗词
INSERT INTO t_poem (title, author, dynasty, content, match_line, pinyin, translation, explanation, keywords, object_name, scene) VALUES
('小池','杨万里','宋','泉眼无声惜细流，树阴照水爱晴柔。小荷才露尖尖角，早有蜻蜓立上头。','小荷才露尖尖角，早有蜻蜓立上头','xiǎo hé cái lù jiān jiān jiǎo, zǎo yǒu qīng tíng lì shàng tóu','小荷刚刚露出尖尖的花苞，就已有蜻蜓立在上面了。','泉水从泉眼里静静地流出来。树影倒映在水中，很喜爱这晴天的柔和。小荷叶才露尖尖角，已有蜻蜓立上头。','荷花,荷叶,池塘,蜻蜓,夏天','荷花','池塘'),
('绝句','杜甫','唐','两个黄鹂鸣翠柳，一行白鹭上青天。窗含西岭千秋雪，门泊东吴万里船。','两个黄鹂鸣翠柳，一行白鹭上青天','liǎng gè huáng lí míng cuì liǔ, yī xíng bái lù shàng qīng tiān','两只黄鹂在翠绿的柳树上婉转啼叫，一行白鹭排着整齐的队伍飞上了蓝天。','两只黄鹂在翠绿的柳树上啼叫，一行白鹭飞上蓝天。窗口望去是西岭千年积雪，门前停着东吴来的万里航船。','鸟,黄鹂,白鹭,柳树,天空,春天','鸟','柳树'),
('春晓','孟浩然','唐','春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。','春眠不觉晓，处处闻啼鸟','chūn mián bù jué xiǎo, chù chù wén tí niǎo','春天的睡眠格外香甜，不知不觉天已经亮了，窗外到处传来鸟儿的啼叫声。','春天的早晨不知不觉天已大亮，窗外处处是鸟儿的啼鸣。回想昨夜风雨声，不知花儿落了多少。','花,春天,早晨,鸟','花','庭院'),
('静夜思','李白','唐','床前明月光，疑是地上霜。举头望明月，低头思故乡。','床前明月光，疑是地上霜','chuáng qián míng yuè guāng, yí shì dì shàng shuāng','明亮的月光洒在床前，好像是地上结了一层洁白的霜。','明亮的月光洒在床前，好像地上铺了一层白霜。抬头望明月，低头思念起远方的故乡。','月亮,月光,夜晚,星空','月亮','夜空'),
('赋得古原草送别','白居易','唐','离离原上草，一岁一枯荣。野火烧不尽，春风吹又生。','离离原上草，一岁一枯荣','lí lí yuán shàng cǎo, yī suì yī kū róng','原野上的草长得非常茂盛，每年都会经历枯萎和繁茂的循环。','原野上的草非常茂盛，每年经历枯萎和繁茂。野火烧不尽它们，春风吹过又会重生。','草,草原,野草,植物,自然','草','原野'),
('登鹳雀楼','王之涣','唐','白日依山尽，黄河入海流。欲穷千里目，更上一层楼。','白日依山尽，黄河入海流','bái rì yī shān jìn, huáng hé rù hǎi liú','太阳靠着山边慢慢落下，黄河水浩浩荡荡地向大海奔流。','太阳靠着山边落下，黄河水向大海奔流。想看到千里之外更远的地方，就要再登上一层楼。','山,夕阳,太阳,河流,黄河','山','山峦'),
('咏鹅','骆宾王','唐','鹅鹅鹅，曲项向天歌。白毛浮绿水，红掌拨清波。','白毛浮绿水，红掌拨清波','bái máo fú lǜ shuǐ, hóng zhǎng bō qīng bō','洁白的羽毛漂浮在碧绿的水面上，红红的脚掌拨动着清清的水波。','大白鹅弯着脖子朝天叫，洁白的羽毛浮在碧绿的水面，红红的脚掌拨动着清清水波。骆宾王七岁时写的诗，充满童趣。','鹅,白鹅,水鸟,湖水','鹅','湖面'),
('咏柳','贺知章','唐','碧玉妆成一树高，万条垂下绿丝绦。不知细叶谁裁出，二月春风似剪刀。','不知细叶谁裁出，二月春风似剪刀','bù zhī xì yè shéi cái chū, èr yuè chūn fēng sì jiǎn dāo','这细细的柳叶是谁裁剪出来的呢？原来是二月里的春风，它就像一把灵巧的剪刀。','高高的柳树好像用碧玉装扮成的，千万条柳枝垂下来像绿色丝带。细细的嫩叶是谁裁剪的？原来是二月的春风。','柳树,柳枝,柳叶,春风,春天','柳树','河岸'),
('江南','汉乐府','汉','江南可采莲，莲叶何田田。鱼戏莲叶间，鱼戏莲叶东，鱼戏莲叶西，鱼戏莲叶南，鱼戏莲叶北。','鱼戏莲叶间','yú xì lián yè jiān','鱼儿在荷叶间欢快地游来游去。','江南正是采莲好时节，池塘里长满了碧绿荷叶。鱼儿在荷叶间自由自在地游来游去。','鱼,小鱼,池塘,水,荷叶','鱼','池塘'),
('宿新市徐公店','杨万里','宋','篱落疏疏一径深，树头新绿未成阴。儿童急走追黄蝶，飞入菜花无处寻。','儿童急走追黄蝶，飞入菜花无处寻','ér tóng jí zǒu zhuī huáng dié, fēi rù cài huā wú chù xún','小朋友飞快地跑着追一只黄蝴蝶，蝴蝶飞进了油菜花丛中就找不到了。','篱笆稀稀疏疏，小路通向远方。小朋友追着黄蝴蝶，蝴蝶飞进金黄油菜花丛就找不到了。','蝴蝶,蜻蜓,昆虫,花丛,儿童','蝴蝶','花园');
