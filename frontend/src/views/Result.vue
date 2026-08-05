<template>
  <div class="result-page">
    <div v-if="!result" class="empty-state">
      <van-empty description="未找到此发现记录" />
      <van-button type="primary" round @click="goHome">返回首页</van-button>
    </div>

    <template v-else>
      <van-nav-bar title="发现详情" left-text="返回" left-arrow @click-left="goHome" fixed placeholder />

      <div class="photo-area">
        <img :src="photoSrc" alt="拍摄的照片" class="photo-image" @error="handleImageError" />
        <div class="photo-gradient" />
      </div>

      <div class="analysis-row" v-if="result.analysis">
        <div class="analysis-tags">
          <span class="tag tag-object">🔍 {{ result.analysis.object }}</span>
          <span class="tag tag-confidence">置信度 {{ (result.analysis.confidence * 100).toFixed(0) }}%</span>
        </div>
      </div>

      <div class="poem-section">
        <PoemCard :result="result" />
      </div>

      <div class="audio-section">
        <AudioPlayer ref="audioRef" :content="result.poemLine"
          @playing="isAudioPlaying = true" @paused="isAudioPlaying = false" @ended="isAudioPlaying = false" />
      </div>

      <ResultActions :switching="switching" :favorited="store.isFavorited" :playing="isAudioPlaying"
        @toggle-play="audioRef?.toggle()" @show-full="showFullSheet = true"
        @switch-poem="handleSwitchPoem" @toggle-fav="handleToggleFav" />

      <div class="share-section">
        <button class="share-btn" :disabled="sharing" @click="handleShare">
          <span class="share-icon">{{ sharing ? '⏳' : '📤' }}</span>
          <span class="share-label">{{ sharing ? '生成中...' : '分享' }}</span>
        </button>
      </div>

      <div class="bottom-spacer" />
    </template>

    <FullPoemSheet v-if="result" v-model:show="showFullSheet" :result="result" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDiscoveryStore } from '../store/discovery.js'
import { showToast } from 'vant'
import PoemCard from '../components/PoemCard.vue'
import AudioPlayer from '../components/AudioPlayer.vue'
import ResultActions from '../components/ResultActions.vue'
import FullPoemSheet from '../components/FullPoemSheet.vue'
import { useShareImage } from '../composables/useShareImage.js'

const router = useRouter()
const store = useDiscoveryStore()
const { generate, share } = useShareImage()

const audioRef = ref(null)
const isAudioPlaying = ref(false)
const switching = ref(false)
const sharing = ref(false)
const showFullSheet = ref(false)

const result = computed(() => store.currentResult)
const photoSrc = computed(() => store.currentImage?.previewUrl || result.value?.photoUrl || '')

async function handleSwitchPoem() {
  if (switching.value) return
  switching.value = true
  audioRef.value?.stop()
  try { await store.switchPoem(); showToast('已为你换了一句 🌿') }
  catch { showToast('切换失败') }
  finally { switching.value = false }
}

function handleToggleFav() {
  store.toggleFavorite()
  showToast(store.isFavorited ? '已收藏 ❤️' : '已取消收藏')
}

function handleImageError(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"><rect fill="%23f0ebe0" width="400" height="300"/><text x="200" y="150" text-anchor="middle" fill="%23b8a898" font-size="48">📷</text></svg>'
}

async function handleShare() {
  if (sharing.value) return
  sharing.value = true
  try {
    const blob = await generate({ photoSrc: photoSrc.value, poemLine: result.value.poemLine, poemSource: result.value.poemSource, date: new Date().toLocaleDateString('zh-CN') })
    await share(blob, result.value.poemSource || '拍遇存')
  } catch { showToast('分享失败') }
  finally { sharing.value = false }
}

function goHome() { audioRef.value?.stop(); store.resetAll(); router.push({ name: 'Camera' }) }
</script>

<style scoped>
.result-page { min-height: 100%; background: linear-gradient(180deg, #fef9ed 0%, #fdf5e6 20%, #faf0dc 100%); padding-bottom: calc(24px + var(--safe-area-bottom)); }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding-top: 120px; gap: 20px; }
.photo-area { position: relative; width: 100%; aspect-ratio: 4/3; overflow: hidden; background: #2a2520; }
.photo-image { width: 100%; height: 100%; object-fit: cover; }
.photo-gradient { position: absolute; bottom: 0; left: 0; right: 0; height: 48px; background: linear-gradient(to bottom, transparent, #fdf5e6); }
.analysis-row { padding: 12px var(--padding-page) 0; position: relative; z-index: 1; }
.analysis-tags { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.tag { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 20px; font-size: var(--font-size-xs); font-weight: 500; }
.tag-object { background: rgba(232,164,64,0.12); color: #b8751a; }
.tag-confidence { background: rgba(100,140,200,0.1); color: #4a7aaa; }
.poem-section { margin-top: 12px; position: relative; z-index: 1; }
.audio-section { margin: 12px var(--padding-page) 0; }
.share-section { margin-top: 16px; padding: 0 var(--padding-page); }
.share-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 14px 0; border: 2px dashed rgba(180,160,140,0.4); border-radius: var(--radius-md); background: rgba(255,255,255,0.5); cursor: pointer; font-family: inherit; -webkit-tap-highlight-color: transparent; }
.share-btn:disabled { opacity: 0.6; }
.share-icon { font-size: 20px; }
.share-label { font-size: var(--font-size-md); font-weight: 600; color: var(--color-text-secondary); }
.bottom-spacer { height: 32px; }
</style>
