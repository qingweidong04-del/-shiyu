<template>
  <!--
    Result.vue — 发现结果页（核心体验页）

    布局：
      用户拍摄照片
      → AI 识别标签
      → 诗歌卡片（诗句 + 拼音 + 出处 + 白话解释）
      → 语音播放器（AudioPlayer）
      → 操作按钮（再念一次 / 查看全文 / 换一句 / 收藏）
      → 全文弹窗
  -->
  <div class="result-page">
    <!-- ===== 空状态 ===== -->
    <div v-if="!result" class="empty-state">
      <van-empty description="未找到此发现记录" />
      <van-button type="primary" round @click="goHome">返回首页</van-button>
    </div>

    <template v-else>
      <!-- ===== 顶部导航 ===== -->
      <van-nav-bar
        title="发现详情"
        left-text="返回"
        left-arrow
        @click-left="goHome"
        fixed
        placeholder
      />

      <!-- ===== 顶部：照片 ===== -->
      <div class="photo-area">
        <img
          :src="photoSrc"
          alt="拍摄的照片"
          class="photo-image"
          @error="handleImageError"
        />
        <div class="photo-gradient" />
      </div>

      <!-- ===== AI 识别标签 ===== -->
      <div class="analysis-row" v-if="result.analysis">
        <div class="analysis-tags">
          <span class="tag tag-object">🔍 {{ result.analysis.object }}</span>
          <span class="tag tag-scene">📍 {{ result.analysis.scene }}</span>
          <span class="tag tag-confidence">
            置信度 {{ (result.analysis.confidence * 100).toFixed(0) }}%
          </span>
        </div>
      </div>

      <!-- ===== 中间：诗歌卡片 ===== -->
      <div class="poem-section">
        <PoemCard :result="result" />
      </div>

      <!-- ===== 语音播放器 ===== -->
      <div class="audio-section">
        <AudioPlayer
          ref="audioRef"
          :content="result.poemLine"
          @playing="onAudioPlaying"
          @paused="onAudioPaused"
          @ended="onAudioEnded"
        />
      </div>

      <!-- ===== 操作按钮栏 ===== -->
      <ResultActions
        :switching="switching"
        :favorited="store.isFavorited"
        :playing="isAudioPlaying"
        @toggle-play="handleTogglePlay"
        @show-full="showFullSheet = true"
        @switch-poem="handleSwitchPoem"
        @toggle-fav="handleToggleFav"
      />

      <!-- ===== 分享按钮 ===== -->
      <div class="share-section">
        <button
          class="share-btn"
          :disabled="sharing"
          @click="handleShare"
        >
          <span class="share-icon">{{ sharing ? '⏳' : '📤' }}</span>
          <span class="share-label">{{ sharing ? '生成中...' : '分享' }}</span>
        </button>
      </div>

      <div class="bottom-spacer" />
    </template>

    <!-- ===== 全文弹窗 ===== -->
    <FullPoemSheet v-model:show="showFullSheet" :result="result" />
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

// ===== 状态 =====

const audioRef = ref(null)
const isAudioPlaying = ref(false)
const switching = ref(false)
const sharing = ref(false)
const showFullSheet = ref(false)

// ===== 计算属性 =====

const result = computed(() => store.currentResult)

const photoSrc = computed(() => {
  return store.currentImage?.previewUrl || result.value?.photoUrl || ''
})

// ===== 音频事件 =====

function onAudioPlaying() { isAudioPlaying.value = true }
function onAudioPaused()  { isAudioPlaying.value = false }
function onAudioEnded()   { isAudioPlaying.value = false }

/** "再念一次" 按钮 → 触发 AudioPlayer 播放/暂停 */
function handleTogglePlay() {
  audioRef.value?.toggle()
}

// ===== 换一句 =====

async function handleSwitchPoem() {
  if (switching.value) return
  switching.value = true
  // 换之前停止朗读
  audioRef.value?.stop()
  try {
    await store.switchPoem()
    showToast('已为你换了一句 🌿')
  } catch (err) {
    showToast('切换失败，请重试')
  } finally {
    switching.value = false
  }
}

// ===== 收藏 =====

function handleToggleFav() {
  store.toggleFavorite()
  showToast(store.isFavorited ? '已收藏 ❤️' : '已取消收藏')
}

// ===== 图片加载失败 =====

function handleImageError(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"><rect fill="%23f0ebe0" width="400" height="300"/><text x="200" y="150" text-anchor="middle" fill="%23b8a898" font-size="48">📷</text></svg>'
}

// ===== 分享 =====

async function handleShare() {
  if (sharing.value) return
  sharing.value = true

  try {
    const data = {
      photoSrc: photoSrc.value,
      poemLine: result.value.poemLine,
      poemSource: result.value.poemSource,
      date: formatShareDate(result.value.createdAt)
    }

    const blob = await generate(data)
    await share(blob, data.poemSource || '拍遇存')
  } catch (err) {
    showToast('分享失败，请重试')
  } finally {
    sharing.value = false
  }
}

function formatShareDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

// ===== 导航 =====

function goHome() {
  audioRef.value?.stop()
  store.resetAll()
  router.push({ name: 'Camera' })
}
</script>

<style scoped>
.result-page {
  min-height: 100%;
  background: linear-gradient(180deg, #fef9ed 0%, #fdf5e6 20%, #faf0dc 100%);
  padding-bottom: calc(24px + var(--safe-area-bottom));
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120px;
  gap: 20px;
}

/* ===== 照片区域 ===== */
.photo-area {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #2a2520;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: linear-gradient(to bottom, transparent, #fdf5e6);
}

/* ===== AI 识别标签 ===== */
.analysis-row {
  padding: 12px var(--padding-page) 0;
  position: relative;
  z-index: 1;
}

.analysis-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: var(--font-size-xs);
  font-weight: 500;
  letter-spacing: 0.5px;
}

.tag-object {
  background: rgba(232, 164, 64, 0.12);
  color: #b8751a;
}

.tag-scene {
  background: rgba(120, 160, 100, 0.12);
  color: #5a7a3a;
}

.tag-confidence {
  background: rgba(100, 140, 200, 0.1);
  color: #4a7aaa;
}

/* ===== 诗歌卡片区域 ===== */
.poem-section {
  margin-top: 12px;
  position: relative;
  z-index: 1;
}

/* ===== 语音播放器 ===== */
.audio-section {
  margin: 12px var(--padding-page) 0;
}

/* ===== 分享按钮 ===== */
.share-section {
  margin-top: 16px;
  padding: 0 var(--padding-page);
}

.share-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 0;
  border: 2px dashed rgba(180, 160, 140, 0.4);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s, border-color 0.2s;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.share-btn:active {
  background: rgba(232, 164, 64, 0.08);
  border-color: var(--color-primary-light);
}

.share-btn:disabled {
  opacity: 0.6;
  pointer-events: none;
}

.share-icon {
  font-size: 20px;
}

.share-label {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 1px;
}

/* ===== 底部留白 ===== */
.bottom-spacer {
  height: 32px;
}
</style>
