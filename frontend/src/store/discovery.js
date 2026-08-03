import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { uploadImage, getPoem, switchPoem as apiSwitchPoem } from '../api/index.js'

/*
 * discoveryStore — 发现流程全局状态
 *
 * 管理拍照→识别→结果→保存 全流程
 *
 * MVP 数据持久化：localStorage
 *   - historyList: 历史发现记录（自动保存）
 *   - favorites:   收藏列表（poemId 数组）
 *
 * 数据结构：
 *   historyItem = {
 *     id, photoBase64, poemLine, poemSource, poemAuthor,
 *     poemExplanation, keywords, createdAt
 *   }
 */

const HISTORY_KEY = 'paiyucun_history'
const FAVORITES_KEY = 'paiyucun_favorites'

export const useDiscoveryStore = defineStore('discovery', () => {
  // ===== 状态 =====

  const status = ref('idle')
  const currentImage = ref(null)
  const currentResult = ref(null)
  const error = ref(null)

  /** 历史记录列表（从 localStorage 初始化） */
  const historyList = ref(loadFromStorage(HISTORY_KEY, []))

  /** 收藏列表（从 localStorage 初始化） */
  const favorites = ref(loadFromStorage(FAVORITES_KEY, []))

  // ===== 计算属性 =====

  const isRecognizing = computed(() => status.value === 'recognizing')
  const hasResult = computed(() => status.value === 'done' && currentResult.value !== null)
  const historyCount = computed(() => historyList.value.length)

  const isFavorited = computed(() => {
    const poemId = currentResult.value?.poemId
    return poemId ? favorites.value.includes(poemId) : false
  })

  // ===== 图片管理 =====

  function setImageData(payload) {
    if (currentImage.value?.previewUrl) {
      URL.revokeObjectURL(currentImage.value.previewUrl)
    }
    currentImage.value = {
      base64: payload.base64,
      file: payload.file,
      previewUrl: payload.previewUrl
    }
  }

  // ===== 识别流程（两步 API 调用）=====

  /**
   * 完整识别流程：
   *   ① POST /api/image/upload  → { imageUrl, object }
   *   ② GET  /api/poem/{object} → 诗词数据
   *   ③ 合并返回
   */
  async function discover(imageBase64) {
    status.value = 'recognizing'
    error.value = null

    try {
      // ① 上传图片，获取 AI 识别结果
      const uploadResult = await uploadImage(imageBase64)

      // ② 根据识别到的物体获取古诗
      const poem = await getPoem(uploadResult.object)

      // ③ 合并为 currentResult
      currentResult.value = {
        id: `d_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
        photoUrl: uploadResult.imageUrl,
        audioUrl: poem.audioUrl || '',
        localPhotoUrl: currentImage.value?.previewUrl || '',
        createdAt: new Date().toISOString(),

        // AI 分析
        analysis: poem.analysis || { object: uploadResult.object },

        // 诗词（从 getPoem 展平）
        poemId: poem.poemId,
        poemLine: poem.poemLine,
        poemPinyin: poem.poemPinyin,
        poemSource: poem.poemSource,
        poemExplanation: poem.poemExplanation,
        translation: poem.translation,
        fullPoem: poem.fullPoem,
        fullPinyin: poem.fullPinyin,
        fullExplanation: poem.fullExplanation,
        poemTitle: poem.poemTitle,
        poemAuthor: poem.poemAuthor,
        poemDynasty: poem.poemDynasty,
        keywords: poem.keywords || [],

        saved: false
      }
      status.value = 'done'
    } catch (err) {
      error.value = err.message || '识别失败，请重试'
      status.value = 'idle'
      throw err
    }
  }

  // ===== 历史记录（localStorage CRUD）=====

  /**
   * 添加一条发现记录到历史
   * MVP: 调用 discover() 成功后自动调用
   */
  function addRecord() {
    if (!currentResult.value) return

    const record = {
      id: currentResult.value.id,
      // 优先存本地拍摄的 base64 缩略图
      photoBase64: currentImage.value?.base64 || '',
      poemLine: currentResult.value.poemLine,
      poemPinyin: currentResult.value.poemPinyin,
      poemSource: currentResult.value.poemSource,
      poemAuthor: currentResult.value.poemAuthor || '',
      poemDynasty: currentResult.value.poemDynasty || '',
      poemExplanation: currentResult.value.poemExplanation,
      poemId: currentResult.value.poemId,
      keywords: currentResult.value.keywords || [],
      analysis: currentResult.value.analysis || null,
      createdAt: currentResult.value.createdAt || new Date().toISOString()
    }

    // 去重：相同 id 不重复添加
    const exists = historyList.value.find((r) => r.id === record.id)
    if (!exists) {
      historyList.value.unshift(record)
      persistStorage(HISTORY_KEY, historyList.value)
    }

    // 标记已保存
    if (currentResult.value) {
      currentResult.value.saved = true
    }
  }

  /** 加载历史记录（从 localStorage） */
  function loadHistory() {
    historyList.value = loadFromStorage(HISTORY_KEY, [])
  }

  /**
   * 删除一条历史记录
   * @param {string} id
   */
  function deleteRecord(id) {
    const idx = historyList.value.findIndex((r) => r.id === id)
    if (idx >= 0) {
      historyList.value.splice(idx, 1)
      persistStorage(HISTORY_KEY, historyList.value)
    }
  }

  /**
   * 清除全部历史记录
   */
  function clearHistory() {
    historyList.value = []
    persistStorage(HISTORY_KEY, [])
  }

  // ===== 保存（向后兼容）=====

  async function saveDiscovery() {
    addRecord()
  }

  // ===== 换一句 =====

  async function switchPoem() {
    const currentId = currentResult.value?.poemId
    if (!currentId) return

    status.value = 'recognizing'
    try {
      const newPoem = await apiSwitchPoem(currentId)

      currentResult.value = {
        ...currentResult.value,
        ...newPoem,
        saved: false
      }
      status.value = 'done'
    } catch (err) {
      error.value = err.message || '切换失败'
      status.value = 'done'
      throw err
    }
  }

  // ===== 收藏 =====

  function toggleFavorite() {
    const poemId = currentResult.value?.poemId
    if (!poemId) return

    const idx = favorites.value.indexOf(poemId)
    if (idx >= 0) {
      favorites.value.splice(idx, 1)
    } else {
      favorites.value.push(poemId)
    }
    persistStorage(FAVORITES_KEY, favorites.value)
  }

  // ===== 重置 =====

  function reset() {
    status.value = 'idle'
    currentResult.value = null
    error.value = null
  }

  function resetAll() {
    reset()
    if (currentImage.value?.previewUrl) {
      URL.revokeObjectURL(currentImage.value.previewUrl)
    }
    currentImage.value = null
  }

  return {
    status, currentImage, currentResult, historyList, favorites, error,
    isRecognizing, hasResult, historyCount, isFavorited,
    setImageData, discover, addRecord, loadHistory,
    deleteRecord, clearHistory,
    saveDiscovery, switchPoem, toggleFavorite,
    reset, resetAll
  }
})

// ===== localStorage 工具函数 =====

function loadFromStorage(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function persistStorage(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data))
  } catch (e) {
    console.warn(`localStorage 写入失败 (${key}):`, e.message)
  }
}
