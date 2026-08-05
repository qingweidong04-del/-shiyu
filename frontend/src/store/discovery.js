import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const HISTORY_KEY = 'paiyucun_history'
const FAVORITES_KEY = 'paiyucun_favorites'

export const useDiscoveryStore = defineStore('discovery', () => {
  const status = ref('idle')
  const currentImage = ref(null)
  const currentResult = ref(null)
  const error = ref(null)
  const historyList = ref(loadFromStorage(HISTORY_KEY, []))
  const favorites = ref(loadFromStorage(FAVORITES_KEY, []))

  const isRecognizing = computed(() => status.value === 'recognizing')
  const hasResult = computed(() => status.value === 'done' && currentResult.value !== null)
  const historyCount = computed(() => historyList.value.length)
  const isFavorited = computed(() => {
    const poemId = currentResult.value?.poemId
    return poemId ? favorites.value.includes(poemId) : false
  })

  function setImageData(payload) {
    if (currentImage.value?.previewUrl) URL.revokeObjectURL(currentImage.value.previewUrl)
    currentImage.value = { base64: payload.base64, file: payload.file, previewUrl: payload.previewUrl }
  }

  async function discover() {
    status.value = 'recognizing'
    error.value = null
    try {
      const { createDiscovery } = await import('../api/index.js')
      const result = await createDiscovery(currentImage.value?.file || currentImage.value?.base64)
      currentResult.value = {
        id: result.id || ('d_' + Date.now()),
        photoUrl: result.photoUrl || result.imageUrl || '',
        poemLine: result.poemLine || result.poem?.content || '春眠不觉晓，处处闻啼鸟',
        poemPinyin: result.poemPinyin || result.poem?.pinyin || 'chūn mián bù jué xiǎo',
        poemSource: result.poemSource || result.poem?.source || '孟浩然《春晓》',
        poemExplanation: result.poemExplanation || result.poem?.translation || '春天的睡眠格外香甜。',
        translation: result.translation || result.poem?.translation || '',
        fullPoem: result.fullPoem || result.poem?.content || '',
        fullPinyin: result.fullPinyin || result.poem?.pinyin || '',
        fullExplanation: result.fullExplanation || result.poem?.translation || '',
        poemTitle: result.poemTitle || result.poem?.title || '',
        poemAuthor: result.poemAuthor || result.poem?.author || '',
        poemDynasty: result.poemDynasty || result.poem?.dynasty || '',
        keywords: result.keywords || [],
        analysis: result.analysis || { object: result.objectName || '未知' },
        audioUrl: result.audioUrl || '',
        localPhotoUrl: currentImage.value?.previewUrl || '',
        saved: false,
        createdAt: new Date().toISOString()
      }
      status.value = 'done'
    } catch (err) {
      error.value = err.message || '识别失败'
      status.value = 'idle'
      throw err
    }
  }

  function addRecord() {
    if (!currentResult.value) return
    const record = { ...currentResult.value, photoBase64: currentImage.value?.base64 || '' }
    const exists = historyList.value.find(r => r.id === record.id)
    if (!exists) { historyList.value.unshift(record); persistStorage(HISTORY_KEY, historyList.value) }
    currentResult.value.saved = true
  }

  function loadHistory() { historyList.value = loadFromStorage(HISTORY_KEY, []) }

  function deleteRecord(id) {
    const idx = historyList.value.findIndex(r => r.id === id)
    if (idx >= 0) { historyList.value.splice(idx, 1); persistStorage(HISTORY_KEY, historyList.value) }
  }

  function clearHistory() { historyList.value = []; persistStorage(HISTORY_KEY, []) }

  async function saveDiscovery() { addRecord() }

  async function switchPoem() {
    const currentId = currentResult.value?.poemId
    if (!currentId) return
    status.value = 'recognizing'
    try {
      const { switchPoem } = await import('../api/index.js')
      const newPoem = await switchPoem(currentId)
      currentResult.value = { ...currentResult.value, ...newPoem, saved: false }
      status.value = 'done'
    } catch (err) { error.value = err.message || '切换失败'; status.value = 'done'; throw err }
  }

  function toggleFavorite() {
    const poemId = currentResult.value?.poemId
    if (!poemId) return
    const idx = favorites.value.indexOf(poemId)
    idx >= 0 ? favorites.value.splice(idx, 1) : favorites.value.push(poemId)
    persistStorage(FAVORITES_KEY, favorites.value)
  }

  function reset() { status.value = 'idle'; currentResult.value = null; error.value = null }
  function resetAll() { reset(); if (currentImage.value?.previewUrl) URL.revokeObjectURL(currentImage.value.previewUrl); currentImage.value = null }

  return { status, currentImage, currentResult, historyList, favorites, error,
    isRecognizing, hasResult, historyCount, isFavorited,
    setImageData, discover, addRecord, loadHistory, deleteRecord, clearHistory,
    saveDiscovery, switchPoem, toggleFavorite, reset, resetAll }
})

function loadFromStorage(key, fallback) {
  try { const raw = localStorage.getItem(key); return raw ? JSON.parse(raw) : fallback } catch { return fallback }
}
function persistStorage(key, data) {
  try { localStorage.setItem(key, JSON.stringify(data)) } catch (e) { console.warn('localStorage error:', e) }
}
