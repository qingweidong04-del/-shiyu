/*
 * mock/poem.js — Mock 诗词数据库
 *
 * 模拟后端 AI 流水线：
 *   图片上传 → AI 识别物体/场景 → 关键词匹配古诗
 *
 * 导出：
 *   analyzeImage(base64)  → { object, scene, confidence }
 *   matchPoem(object)      → 匹配的诗词条目
 *   discoverFromImage(base64) → 完整流水线，一次调用返回全部结果
 */

// ===== 诗词数据库 =====
// 每条记录对应一种可识别的自然物体

const poemDB = [
  {
    id: 'poem_lotus',
    keywords: ['荷花', '荷叶', '莲花', '池塘', '蜻蜓'],
    object: '荷花',
    scene: '池塘',
    title: '小池',
    author: '杨万里',
    dynasty: '宋',
    matchLine: '小荷才露尖尖角，早有蜻蜓立上头',
    pinyin: 'xiǎo hé cái lù jiān jiān jiǎo, zǎo yǒu qīng tíng lì shàng tóu',
    content: '泉眼无声惜细流，\n树阴照水爱晴柔。\n小荷才露尖尖角，\n早有蜻蜓立上头。',
    fullPinyin: 'quán yǎn wú shēng xī xì liú,\nshù yīn zhào shuǐ ài qíng róu.\nxiǎo hé cái lù jiān jiān jiǎo,\nzǎo yǒu qīng tíng lì shàng tóu.',
    translation: '小荷刚刚露出尖尖的花苞，就已有蜻蜓立在上面了。',
    explanation: '泉水从泉眼里静静地流出来，好像很爱惜这细细的水流。树影倒映在水中，似乎很喜爱这晴天的柔和。池塘里的小荷叶才刚刚露出尖尖的角，就已经有蜻蜓站在了上面。这首诗写出了初夏池塘边的宁静和生机。'
  },
  {
    id: 'poem_bird',
    keywords: ['鸟', '黄鹂', '白鹭', '麻雀', '燕子', '树枝', '柳树', '天空'],
    object: '鸟',
    scene: '柳树',
    title: '绝句',
    author: '杜甫',
    dynasty: '唐',
    matchLine: '两个黄鹂鸣翠柳，一行白鹭上青天',
    pinyin: 'liǎng gè huáng lí míng cuì liǔ, yī xíng bái lù shàng qīng tiān',
    content: '两个黄鹂鸣翠柳，\n一行白鹭上青天。\n窗含西岭千秋雪，\n门泊东吴万里船。',
    fullPinyin: 'liǎng gè huáng lí míng cuì liǔ,\nyī xíng bái lù shàng qīng tiān.\nchuāng hán xī lǐng qiān qiū xuě,\nmén bó dōng wú wàn lǐ chuán.',
    translation: '两只黄鹂在翠绿的柳树上婉转啼叫，一行白鹭排着整齐的队伍飞上了蓝天。',
    explanation: '两只黄鹂在翠绿的柳树上婉转啼叫，一行白鹭排着整齐的队伍飞上了蓝天。从窗口望去，西边山岭上覆盖着千年不化的积雪，门前停泊着从万里之外的东吴开来的船只。诗人用四句话画出了四幅美丽的画面。'
  },
  {
    id: 'poem_spring',
    keywords: ['花', '春天', '早晨', '风雨', '落花'],
    object: '花',
    scene: '庭院',
    title: '春晓',
    author: '孟浩然',
    dynasty: '唐',
    matchLine: '春眠不觉晓，处处闻啼鸟',
    pinyin: 'chūn mián bù jué xiǎo, chù chù wén tí niǎo',
    content: '春眠不觉晓，\n处处闻啼鸟。\n夜来风雨声，\n花落知多少。',
    fullPinyin: 'chūn mián bù jué xiǎo,\nchù chù wén tí niǎo.\nyè lái fēng yǔ shēng,\nhuā luò zhī duō shǎo.',
    translation: '春天的睡眠格外香甜，不知不觉天已经亮了，窗外到处传来鸟儿的啼叫声。',
    explanation: '春天的早晨，不知不觉天已大亮。窗外处处是鸟儿的啼鸣。回想昨夜的风雨声，不知道花儿落了多少。这首诗写出了诗人对春天早晨的喜爱，也表达了对落花的怜惜之情。'
  },
  {
    id: 'poem_moon',
    keywords: ['月亮', '月光', '夜晚', '星空'],
    object: '月亮',
    scene: '夜空',
    title: '静夜思',
    author: '李白',
    dynasty: '唐',
    matchLine: '床前明月光，疑是地上霜',
    pinyin: 'chuáng qián míng yuè guāng, yí shì dì shàng shuāng',
    content: '床前明月光，\n疑是地上霜。\n举头望明月，\n低头思故乡。',
    fullPinyin: 'chuáng qián míng yuè guāng,\nyí shì dì shàng shuāng.\njǔ tóu wàng míng yuè,\ndī tóu sī gù xiāng.',
    translation: '明亮的月光洒在床前，好像是地上结了一层洁白的霜。',
    explanation: '明亮的月光洒在床前，好像地上铺了一层白霜。抬起头望着天上的明月，低下头不禁思念起远方的故乡。这是李白最脍炙人口的一首诗，写出了游子对家乡深深的思念。'
  },
  {
    id: 'poem_grass',
    keywords: ['草', '草原', '野草', '植物', '自然', '野花'],
    object: '草',
    scene: '原野',
    title: '赋得古原草送别',
    author: '白居易',
    dynasty: '唐',
    matchLine: '离离原上草，一岁一枯荣',
    pinyin: 'lí lí yuán shàng cǎo, yī suì yī kū róng',
    content: '离离原上草，\n一岁一枯荣。\n野火烧不尽，\n春风吹又生。',
    fullPinyin: 'lí lí yuán shàng cǎo,\nyī suì yī kū róng.\nyě huǒ shāo bù jìn,\nchūn fēng chuī yòu shēng.',
    translation: '原野上的草长得非常茂盛，每年都会经历枯萎和繁茂的循环。',
    explanation: '原野上的草长得非常茂盛，每年都会经历枯萎和繁茂的循环。野火永远烧不尽它们，只要春风吹过，又会生机勃勃地生长起来。这首诗赞美了野草顽强的生命力。'
  },
  {
    id: 'poem_mountain',
    keywords: ['山', '夕阳', '太阳', '河流', '黄河', '落日'],
    object: '山',
    scene: '山峦',
    title: '登鹳雀楼',
    author: '王之涣',
    dynasty: '唐',
    matchLine: '白日依山尽，黄河入海流',
    pinyin: 'bái rì yī shān jìn, huáng hé rù hǎi liú',
    content: '白日依山尽，\n黄河入海流。\n欲穷千里目，\n更上一层楼。',
    fullPinyin: 'bái rì yī shān jìn,\nhuáng hé rù hǎi liú.\nyù qióng qiān lǐ mù,\ngèng shàng yī céng lóu.',
    translation: '太阳靠着山边慢慢落下，黄河水浩浩荡荡地向大海奔流。',
    explanation: '太阳靠着山边慢慢落下，黄河水浩浩荡荡地向大海奔流。想要看到千里之外更远的地方，就要再登上一层楼。这首诗告诉我们要不断向上攀登，才能看到更广阔的世界。'
  },
  {
    id: 'poem_goose',
    keywords: ['鹅', '白鹅', '水鸟', '湖水'],
    object: '鹅',
    scene: '湖面',
    title: '咏鹅',
    author: '骆宾王',
    dynasty: '唐',
    matchLine: '白毛浮绿水，红掌拨清波',
    pinyin: 'bái máo fú lǜ shuǐ, hóng zhǎng bō qīng bō',
    content: '鹅，鹅，鹅，\n曲项向天歌。\n白毛浮绿水，\n红掌拨清波。',
    fullPinyin: 'é, é, é,\nqū xiàng xiàng tiān gē.\nbái máo fú lǜ shuǐ,\nhóng zhǎng bō qīng bō.',
    translation: '洁白的羽毛漂浮在碧绿的水面上，红红的脚掌拨动着清清的水波。',
    explanation: '大白鹅弯着脖子朝天叫着，好像在唱歌一样。洁白的羽毛浮在碧绿的水面上，红红的脚掌拨动着清清的水波。这是骆宾王七岁时写的诗，充满童趣，是小朋友们最喜欢的古诗之一。'
  },
  {
    id: 'poem_willow',
    keywords: ['柳树', '柳枝', '柳叶', '春风', '春天'],
    object: '柳树',
    scene: '河岸',
    title: '咏柳',
    author: '贺知章',
    dynasty: '唐',
    matchLine: '不知细叶谁裁出，二月春风似剪刀',
    pinyin: 'bù zhī xì yè shéi cái chū, èr yuè chūn fēng sì jiǎn dāo',
    content: '碧玉妆成一树高，\n万条垂下绿丝绦。\n不知细叶谁裁出，\n二月春风似剪刀。',
    fullPinyin: 'bì yù zhuāng chéng yī shù gāo,\nwàn tiáo chuí xià lǜ sī tāo.\nbù zhī xì yè shéi cái chū,\nèr yuè chūn fēng sì jiǎn dāo.',
    translation: '这细细的柳叶是谁裁剪出来的呢？原来是二月里的春风，它就像一把灵巧的剪刀。',
    explanation: '高高的柳树好像是用碧玉装扮成的，千万条柳枝垂下来就像绿色的丝带。这细细的嫩叶是谁裁剪出来的呢？原来是二月的春风，它就像一把灵巧的剪刀。诗人把春风比作剪刀，想象力非常奇妙！'
  },
  {
    id: 'poem_fish',
    keywords: ['鱼', '小鱼', '金鱼', '池塘', '水'],
    object: '鱼',
    scene: '池塘',
    title: '江南',
    author: '汉乐府',
    dynasty: '汉',
    matchLine: '鱼戏莲叶间',
    pinyin: 'yú xì lián yè jiān',
    content: '江南可采莲，\n莲叶何田田。\n鱼戏莲叶间，\n鱼戏莲叶东，\n鱼戏莲叶西，\n鱼戏莲叶南，\n鱼戏莲叶北。',
    fullPinyin: 'jiāng nán kě cǎi lián,\nlián yè hé tián tián.\nyú xì lián yè jiān,\nyú xì lián yè dōng,\nyú xì lián yè xī,\nyú xì lián yè nán,\nyú xì lián yè běi.',
    translation: '鱼儿在荷叶间欢快地游来游去。',
    explanation: '江南正是采莲的好时节，池塘里长满了碧绿的荷叶。鱼儿在荷叶间自由自在地游来游去——一会儿游到东边，一会儿游到西边，一会儿游到南边，一会儿游到北边。这首诗用简单的重复写出了鱼儿嬉戏的快乐。'
  },
  {
    id: 'poem_butterfly',
    keywords: ['蝴蝶', '蜻蜓', '昆虫', '花丛'],
    object: '蝴蝶',
    scene: '花园',
    title: '宿新市徐公店',
    author: '杨万里',
    dynasty: '宋',
    matchLine: '儿童急走追黄蝶，飞入菜花无处寻',
    pinyin: 'ér tóng jí zǒu zhuī huáng dié, fēi rù cài huā wú chù xún',
    content: '篱落疏疏一径深，\n树头新绿未成阴。\n儿童急走追黄蝶，\n飞入菜花无处寻。',
    fullPinyin: 'lí luò shū shū yī jìng shēn,\nshù tóu xīn lǜ wèi chéng yīn.\nér tóng jí zǒu zhuī huáng dié,\nfēi rù cài huā wú chù xún.',
    translation: '小朋友飞快地跑着追一只黄蝴蝶，蝴蝶飞进了油菜花丛中就找不到了。',
    explanation: '篱笆稀稀疏疏的，一条小路通向远方。树上的新叶还没有长成浓密的树荫。一个小朋友飞快地跑着追一只黄色的蝴蝶，可是蝴蝶飞进金黄的油菜花丛中，就再也找不到了。这首诗写出了乡间春天里小朋友玩耍的有趣场景。'
  }
]

// ===== 模拟 AI 图片分析 =====

/**
 * 模拟 AI 识别图片中的物体
 *
 * 在真实场景中，这会调用 OpenAI Vision 或类似服务。
 * Mock 实现：基于图片特征（如 Base64 长度）从数据库中选一个结果，
 * 模拟出有一定"识别准确率"的效果。
 *
 * @param {string} imageBase64 图片 Base64 数据
 * @returns {{ object: string, scene: string, confidence: number, matchedId: string }}
 */
export function analyzeImage(imageBase64) {
  // 模拟 AI 处理延迟
  const hash = imageBase64 ? imageBase64.length : 0

  // 用图片数据的哈希特征来"模拟"不同识别结果
  // 让同一张图片总是返回相同结果（模拟确定性 AI）
  const index = hash % poemDB.length
  const entry = poemDB[index]

  // 模拟置信度（0.70 ~ 0.99 之间波动）
  const confidence = 0.7 + ((hash * 7 + index * 13) % 30) / 100

  return {
    object: entry.object,
    scene: entry.scene,
    confidence: Math.round(confidence * 100) / 100,
    matchedId: entry.id
  }
}

// ===== 诗词匹配 =====

/**
 * 根据识别到的物体名称匹配古诗
 *
 * @param {string} object AI 识别到的物体（如 "荷花"）
 * @returns {object|null} 匹配的诗词数据，未匹配则返回 null
 */
export function matchPoem(object) {
  if (!object) return null

  // 精确匹配 object 字段
  let entry = poemDB.find(
    (p) => p.object === object
  )

  // 降级：关键词模糊匹配
  if (!entry) {
    entry = poemDB.find(
      (p) => p.keywords.some((kw) => object.includes(kw) || kw.includes(object))
    )
  }

  // 仍未匹配则返回第一首
  if (!entry) {
    entry = poemDB[0]
  }

  return {
    poemId: entry.id,
    title: entry.title,
    author: entry.author,
    dynasty: entry.dynasty,
    matchLine: entry.matchLine,
    pinyin: entry.pinyin,
    content: entry.content,
    fullPinyin: entry.fullPinyin,
    translation: entry.translation,
    explanation: entry.explanation,
    keywords: entry.keywords
  }
}

// ===== 完整发现流水线 =====

/**
 * 模拟完整的"拍照→AI识别→匹配古诗"流程
 *
 * 这是后端 /api/discover 接口的 Mock 实现。
 * 流程：analyzeImage → matchPoem → 合并返回
 *
 * @param {string} imageBase64 图片 Base64
 * @returns {Promise<object>} 完整的发现结果
 */
export async function discoverFromImage(imageBase64) {
  // 模拟网络延迟（1~1.5 秒）
  const delay = 800 + Math.random() * 700
  await new Promise((r) => setTimeout(r, delay))

  // ① AI 识别
  const analysis = analyzeImage(imageBase64)

  // ② 匹配古诗
  const poem = matchPoem(analysis.object)

  if (!poem) {
    throw new Error('未找到匹配的古诗，请试试拍摄其他自然物体')
  }

  // ③ 合并返回
  const discoveryId = `d_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`

  return {
    // 发现记录
    id: discoveryId,
    createdAt: new Date().toISOString(),

    // AI 分析结果
    analysis: {
      object: analysis.object,
      scene: analysis.scene,
      confidence: analysis.confidence
    },

    // 匹配的诗词
    poem: {
      poemId: poem.poemId,
      title: poem.title,
      author: poem.author,
      dynasty: poem.dynasty,
      matchLine: poem.matchLine,
      pinyin: poem.pinyin,
      content: poem.content,
      fullPinyin: poem.fullPinyin,
      translation: poem.translation,
      explanation: poem.explanation,
      keywords: poem.keywords
    },

    // 照片 URL（Mock：随机占位图）
    photoUrl: `https://picsum.photos/seed/${discoveryId}/400/400`,

    // 音频（暂无，由浏览器 TTS 降级）
    audioUrl: ''
  }
}

/**
 * 换一句诗（排除当前 poemId）
 * @param {string} currentPoemId 当前诗 ID
 * @returns {Promise<object>}
 */
export async function switchToAnotherPoem(currentPoemId) {
  await new Promise((r) => setTimeout(r, 500))

  const candidates = poemDB.filter((p) => p.id !== currentPoemId)
  const entry = candidates[Math.floor(Math.random() * candidates.length)]

  return {
    poemId: entry.id,
    title: entry.title,
    author: entry.author,
    dynasty: entry.dynasty,
    matchLine: entry.matchLine,
    pinyin: entry.pinyin,
    content: entry.content,
    fullPinyin: entry.fullPinyin,
    translation: entry.translation,
    explanation: entry.explanation,
    keywords: entry.keywords
  }
}

export { poemDB }
