/*
 * api/mock.js — Mock API 实现
 *
 * 模拟后端所有接口。当 VITE_USE_MOCK !== 'false' 时生效。
 * 每个函数签名与真实 API 完全一致，确保切换时只需改 api/index.js。
 */

import { discoverFromImage, switchToAnotherPoem, poemDB, matchPoem } from '../mock/poem.js'
// discoverFromImage: used by uploadImage mock
// matchPoem: used by getPoem mock

// ===== 模拟延迟 =====
const delay = (ms = 500) => new Promise((r) => setTimeout(r, ms))

// ===== 完整发现流程（Mock）=====

export async function createDiscovery(file) {
  const base64 = typeof file === 'string' ? file : ''
  const raw = await discoverFromImage(base64)
  return {
    id: raw.id,
    imageUrl: raw.photoUrl,
    objectName: raw.analysis.object,
    confidence: raw.analysis.confidence,
    poem: {
      title: raw.poem.title,
      content: raw.poem.matchLine,
      author: raw.poem.author,
      dynasty: raw.poem.dynasty,
      pinyin: raw.poem.pinyin,
      translation: raw.poem.translation,
      source: raw.poem.author + '《' + raw.poem.title + '》'
    }
  }
}

// ===== 图片上传接口 =====

export async function uploadImage(file) {
  await delay(800)

  // 从 mock/poem.js 的流水线获取识别结果
  const base64 = typeof file === 'string' ? file : ''
  const raw = await discoverFromImage(base64)

  return {
    imageUrl: raw.photoUrl,
    object: raw.analysis.object
  }
}

// ===== 获取诗接口 =====
// GET /api/poem/{object}
// 返回: poem 完整数据

export async function getPoem(object = '') {
  await delay(400)

  // 根据 AI 识别到的 object 精确匹配诗词
  const poem = matchPoem(object) || matchPoem('荷花') // 兜底

  return {
    poemId: poem.poemId,
    poemLine: poem.matchLine,
    poemPinyin: poem.pinyin,
    poemSource: `${poem.author}《${poem.title}》`,
    poemExplanation: poem.explanation,
    translation: poem.translation,
    fullPoem: poem.content,
    fullPinyin: poem.fullPinyin,
    fullExplanation: poem.explanation,
    poemTitle: poem.title,
    poemAuthor: poem.author,
    poemDynasty: poem.dynasty,
    keywords: poem.keywords,
    audioUrl: '',
    analysis: { object, scene: poem.keywords[1] || '', confidence: 0.92 }
  }
}

// ===== 换一句诗 =====
// GET /api/poem/random?exclude={id}

export async function switchPoem(currentId) {
  await delay(400)
  const entry = await switchToAnotherPoem(currentId)

  return {
    poemId: entry.poemId,
    poemLine: entry.matchLine,
    poemPinyin: entry.pinyin,
    poemSource: `${entry.author}《${entry.title}》`,
    poemExplanation: entry.explanation,
    translation: entry.translation,
    fullPoem: entry.content,
    fullPinyin: entry.fullPinyin,
    fullExplanation: entry.explanation,
    poemTitle: entry.title,
    poemAuthor: entry.author,
    poemDynasty: entry.dynasty,
    keywords: entry.keywords
  }
}

// ===== 保存发现 =====
// POST /api/discoveries

const mockHistory = []

export async function saveDiscovery(data) {
  await delay(200)
  const id = data.id || `d_${Date.now()}`
  mockHistory.unshift({ ...data, id, savedAt: new Date().toISOString() })
  return { success: true, id }
}

// ===== 获取历史列表 =====
// GET /api/discoveries?page=&size=

export async function getDiscoveries(page = 1, size = 20) {
  await delay(400)
  // 优先返回本地保存的，否则返回预置数据
  if (mockHistory.length > 0) {
    return { list: mockHistory.slice(0, size), total: mockHistory.length }
  }

  // 预置示例数据
  const list = poemDB.slice(0, 4).map((entry, i) => ({
    id: `d_sample_${i}`,
    photoUrl: `https://picsum.photos/seed/history${i}/200/200`,
    poemLine: entry.matchLine,
    poemSource: `${entry.author}《${entry.title}》`,
    poemExplanation: entry.explanation,
    keywords: entry.keywords,
    createdAt: new Date(Date.now() - i * 86400000).toISOString()
  }))

  return { list, total: list.length }
}
