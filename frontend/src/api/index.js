/*
 * api/index.js — API 统一入口
 *
 * VITE_USE_MOCK=true  → 使用 mock
 * VITE_USE_MOCK=false → 使用真实后端
 */
import http from './http.js'
import * as mock from './mock.js'

const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

// ===== 完整发现流程 =====

/**
 * POST /api/discovery/create
 * 一次调用完成：上传图片 → AI识别 → 匹配诗词 → 保存记录
 *
 * @param {File} file - 图片文件
 * @returns {Promise<object>} { imageUrl, objectName, poem: {...} }
 */
export async function createDiscovery(file) {
  if (USE_MOCK) return mock.createDiscovery(file)

  const formData = new FormData()
  formData.append('image', file)
  const result = await http.post('/api/discovery/create', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  // 转换为前端 Store 期望的格式
  return normalizeResult(result)
}

// ===== 图片上传（独立使用）=====

export function uploadImage(file) {
  if (USE_MOCK) return mock.uploadImage(file)

  const formData = new FormData()
  formData.append('image', file)
  return http.post('/api/image/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ===== 诗词查询 =====

export function getPoem(object) {
  if (USE_MOCK) return mock.getPoem(object)
  return http.get('/api/poem', { params: { object } })
}

// ===== 换一句 =====

export async function switchPoem(currentId) {
  if (USE_MOCK) return mock.switchPoem(currentId)
  // 后端返回 PoemVO 格式，转为 Store 认识的扁平格式
  const raw = await http.get('/api/poem/random', { params: { exclude: currentId } })
  return {
    poemId: raw.poemId,
    poemLine: raw.content,
    poemPinyin: raw.pinyin,
    poemSource: raw.author + '《' + raw.title + '》',
    poemExplanation: raw.translation,
    translation: raw.translation,
    fullPoem: raw.fullContent || raw.content,
    fullPinyin: raw.fullPinyin || raw.pinyin,
    fullExplanation: raw.fullExplanation || raw.translation,
    poemTitle: raw.title,
    poemAuthor: raw.author,
    poemDynasty: raw.dynasty
  }
}

// ===== 保存发现 =====

export function saveDiscovery(data) {
  if (USE_MOCK) return mock.saveDiscovery(data)
  return http.post('/api/discovery/save', data)
}

// ===== 历史列表 =====

export function getDiscoveries(page = 1, size = 20) {
  if (USE_MOCK) return mock.getDiscoveries(page, size)
  return http.get('/api/discovery/list', { params: { page, size } })
}

// ===== 工具：将后端返回格式转为前端 Store 格式 =====

function normalizeResult(result) {
  const poem = result.poem || {}
  return {
    id: `d_${result.id || Date.now()}`,
    photoUrl: result.imageUrl,
    audioUrl: '',
    createdAt: new Date().toISOString(),
    analysis: {
      object: result.objectName,
      confidence: result.confidence
    },
    poemId: poem.poemId || null,
    poemLine: poem.content || '',
    poemPinyin: poem.pinyin || '',
    poemSource: poem.source || '',
    poemExplanation: poem.translation || '',
    translation: poem.translation || '',
    fullPoem: poem.fullContent || poem.content || '',
    fullPinyin: poem.fullPinyin || poem.pinyin || '',
    fullExplanation: poem.fullExplanation || poem.translation || '',
    poemTitle: poem.title || '',
    poemAuthor: poem.author || '',
    poemDynasty: poem.dynasty || '',
    keywords: [],
    saved: false
  }
}

export default http
