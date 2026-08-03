/*
 * api/index.js — API 统一入口
 *
 * 根据环境变量 VITE_USE_MOCK 自动切换：
 *   true / 未设置  → 使用 mock 实现
 *   false          → 使用真实 HTTP 请求
 *
 * 使用方式：
 *   import { uploadImage, getPoem } from '@/api'
 *   const { imageUrl, object } = await uploadImage(file)
 *   const poem = await getPoem(object)
 *
 * 切换到真实后端：
 *   修改 .env.production 中 VITE_USE_MOCK=false 即可，
 *   无需改动任何业务代码。
 */

import http from './http.js'
import * as mock from './mock.js'

// 是否使用 Mock
const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

// ===== 图片上传 + AI 识别 =====

/**
 * POST /api/image/upload
 * 上传图片，返回 AI 识别的物体
 *
 * @param {File|string} file - File 对象或 Base64 字符串
 * @returns {Promise<{ imageUrl: string, object: string }>}
 */
export function uploadImage(file) {
  if (USE_MOCK) return mock.uploadImage(file)

  const formData = new FormData()
  formData.append('file', file)
  return http.post('/api/image/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ===== 获取诗句 =====

/**
 * GET /api/poem/{object}
 * 根据 AI 识别的物体名称获取匹配的古诗
 *
 * @param {string} object - 物体名称，如 "荷花"
 * @returns {Promise<object>} 诗词完整数据
 */
export function getPoem(object) {
  if (USE_MOCK) return mock.getPoem(object)
  return http.get(`/api/poem/${encodeURIComponent(object)}`)
}

// ===== 换一句 =====

/**
 * GET /api/poem/random?exclude={id}
 * 随机获取另一首诗（排除当前）
 *
 * @param {string} currentId - 当前诗的 poemId
 * @returns {Promise<object>}
 */
export function switchPoem(currentId) {
  if (USE_MOCK) return mock.switchPoem(currentId)
  return http.get('/api/poem/random', { params: { exclude: currentId } })
}

// ===== 保存发现 =====

/**
 * POST /api/discoveries
 * 保存一条发现记录
 *
 * @param {object} data - 发现记录数据
 * @returns {Promise<{ success: boolean, id: string }>}
 */
export function saveDiscovery(data) {
  if (USE_MOCK) return mock.saveDiscovery(data)
  return http.post('/api/discoveries', data)
}

// ===== 获取历史列表 =====

/**
 * GET /api/discoveries
 * 分页获取历史发现列表
 *
 * @param {number} page
 * @param {number} size
 * @returns {Promise<{ list: array, total: number }>}
 */
export function getDiscoveries(page = 1, size = 20) {
  if (USE_MOCK) return mock.getDiscoveries(page, size)
  return http.get('/api/discoveries', { params: { page, size } })
}

export default http
