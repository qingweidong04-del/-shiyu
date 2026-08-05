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

export function switchPoem(currentId) {
  if (USE_MOCK) return mock.switchPoem(currentId)
  return http.get('/api/poem/random', { params: { exclude: currentId } })
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
    poemId: null,
    poemLine: poem.content || '',
    poemPinyin: poem.pinyin || '',
    poemSource: poem.source || '',
    poemExplanation: poem.translation || '',
    translation: poem.translation || '',
    fullPoem: poem.content || '',
    fullPinyin: poem.pinyin || '',
    fullExplanation: poem.translation || '',
    poemTitle: poem.title || '',
    poemAuthor: poem.author || '',
    poemDynasty: poem.dynasty || '',
    keywords: [],
    saved: false
  }
}

export default http
