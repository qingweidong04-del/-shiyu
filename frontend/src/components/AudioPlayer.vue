<template>
  <!--
    AudioPlayer.vue — 语音朗读播放器
    基于浏览器 SpeechSynthesis API，无需后端 TTS
    支持：播放、暂停、继续、重新播放
  -->
  <div class="audio-player">
    <!-- 控制栏 -->
    <div class="controls">
      <!-- 播放/暂停 -->
      <button
        class="ctrl-btn ctrl-btn--play"
        :disabled="!supported"
        @click="toggle"
      >
        <span class="ctrl-icon">{{ playIcon }}</span>
        <span class="ctrl-label">{{ playLabel }}</span>
      </button>

      <!-- 重新播放 -->
      <button
        class="ctrl-btn ctrl-btn--replay"
        :disabled="!supported || status === 'idle'"
        @click="replay"
      >
        <span class="ctrl-icon">↺</span>
        <span class="ctrl-label">重播</span>
      </button>
    </div>

    <!-- 状态指示 -->
    <div class="status-row" v-if="status !== 'idle'">
      <div class="sound-waves" :class="{ 'is-active': status === 'playing' }">
        <span class="wave" v-for="i in 5" :key="i" />
      </div>
      <span class="status-text">{{ statusText }}</span>
    </div>

    <!-- 不支持提示 -->
    <p v-if="!supported" class="hint">
      ⚠️ 当前浏览器不支持语音朗读，请使用 Chrome 或 Edge
    </p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'

// ===== Props =====

const props = defineProps({
  /** 要朗读的文本内容 */
  content: { type: String, default: '' },
  /** 朗读速率 0.1~2.0，儿童建议 0.6~0.8 */
  rate: { type: Number, default: 0.7 },
  /** 音调 0~2，稍高适合儿童 */
  pitch: { type: Number, default: 1.1 },
  /** 语言 */
  lang: { type: String, default: 'zh-CN' }
})

// ===== Emits =====

const emit = defineEmits(['playing', 'paused', 'ended', 'error'])

// ===== 状态 =====

const status = ref('idle')       // 'idle' | 'playing' | 'paused'
const supported = ref(true)      // 浏览器是否支持
const voiceName = ref('')        // 当前使用的语音名称

let currentUtterance = null      // 当前 SpeechSynthesisUtterance 实例

// ===== 计算属性 =====

const playIcon = computed(() => {
  if (status.value === 'playing') return '⏸'
  if (status.value === 'paused')  return '▶'
  return '▶'
})

const playLabel = computed(() => {
  if (status.value === 'playing') return '暂停'
  if (status.value === 'paused')  return '继续'
  return '播放'
})

const statusText = computed(() => {
  if (status.value === 'playing') return '正在朗诵...'
  if (status.value === 'paused')  return '已暂停'
  return ''
})

// ===== 核心方法 =====

/** 播放/暂停切换 */
function toggle() {
  if (!supported.value) return

  if (status.value === 'playing') {
    pause()
  } else if (status.value === 'paused') {
    resume()
  } else {
    play()
  }
}

/** 开始播放（从头开始） */
function play() {
  if (!props.content || !supported.value) return

  // 先取消任何进行中的朗读
  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(props.content)
  utterance.lang = props.lang
  utterance.rate = props.rate
  utterance.pitch = props.pitch

  // 尝试选择中文语音
  const voices = window.speechSynthesis.getVoices()
  const zhVoice = findBestVoice(voices)
  if (zhVoice) {
    utterance.voice = zhVoice
    voiceName.value = zhVoice.name
  }

  // 事件绑定
  utterance.onstart = () => {
    status.value = 'playing'
    emit('playing')
  }
  utterance.onpause = () => {
    status.value = 'paused'
    emit('paused')
  }
  utterance.onresume = () => {
    status.value = 'playing'
    emit('playing')
  }
  utterance.onend = () => {
    status.value = 'idle'
    currentUtterance = null
    emit('ended')
  }
  utterance.onerror = (e) => {
    // speechSynthesis 在重复调用时会抛 'interrupted' 错误，这是正常的
    if (e.error !== 'interrupted') {
      console.warn('SpeechSynthesis error:', e.error)
      supported.value = false
      emit('error', e)
    }
    status.value = 'idle'
    currentUtterance = null
  }

  currentUtterance = utterance
  window.speechSynthesis.speak(utterance)
}

/** 暂停 */
function pause() {
  if (status.value !== 'playing') return
  window.speechSynthesis.pause()
}

/** 继续 */
function resume() {
  if (status.value !== 'paused') return
  window.speechSynthesis.resume()
}

/** 重新播放（从头开始） */
function replay() {
  window.speechSynthesis.cancel()
  status.value = 'idle'
  currentUtterance = null
  // 小延迟确保 cancel 生效
  setTimeout(() => play(), 50)
}

/** 停止播放 */
function stop() {
  window.speechSynthesis.cancel()
  status.value = 'idle'
  currentUtterance = null
}

// ===== 语音选择 =====

/** 在可用语音列表中选择最佳中文语音 */
function findBestVoice(voices) {
  // 优先级：zh-CN 原生 > zh-CN > zh > 任意含中文名的
  const candidates = voices.filter((v) => v.lang.startsWith('zh'))
  if (candidates.length === 0) return null

  // 优先选择 Google 或 Ting-Ting（macOS 优质中文语音）
  const preferred = candidates.find(
    (v) => v.name.includes('Google') || v.name.includes('Ting-Ting')
  )
  return preferred || candidates[0]
}

// ===== 生命周期 =====

/** 初始化：检测支持 + 预加载语音列表 */
function init() {
  if (!window.speechSynthesis) {
    supported.value = false
    return
  }

  // Chrome 需要调用 getVoices 才能触发 voiceschanged
  const voices = window.speechSynthesis.getVoices()
  if (voices.length > 0) {
    findBestVoice(voices)
  }

  // 部分浏览器异步加载语音列表
  window.speechSynthesis.onvoiceschanged = () => {
    const updated = window.speechSynthesis.getVoices()
    findBestVoice(updated)
  }
}

init()

/** 内容切换时自动停止 */
watch(
  () => props.content,
  () => {
    stop()
  }
)

/** 组件卸载时停止朗读 */
onUnmounted(() => {
  stop()
})

// ===== 暴露方法（供父组件通过 ref 控制）=====
defineExpose({ play, pause, resume, replay, stop, toggle, status })
</script>

<style scoped>
.audio-player {
  padding: 12px var(--padding-card);
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* ===== 按钮栏 ===== */
.controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.ctrl-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-family: inherit;
  font-size: var(--font-size-sm);
  font-weight: 600;
  transition: transform 0.12s, opacity 0.2s;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.ctrl-btn:active {
  transform: scale(0.95);
}

.ctrl-btn:disabled {
  opacity: 0.4;
  pointer-events: none;
}

.ctrl-icon {
  font-size: 18px;
}

/* 播放/暂停按钮 — 暖橙色 */
.ctrl-btn--play {
  background: rgba(232, 164, 64, 0.15);
  color: #b8751a;
}

/* 重播按钮 — 暖棕色 */
.ctrl-btn--replay {
  background: rgba(140, 110, 80, 0.08);
  color: #7a6040;
}

/* ===== 状态指示 ===== */
.status-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
}

.status-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

/* 声波动画 */
.sound-waves {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 16px;
}

.wave {
  width: 3px;
  border-radius: 2px;
  background: var(--color-primary-light);
  transition: height 0.15s;
  height: 4px;
}

.sound-waves.is-active .wave {
  animation: waveAnim 0.8s ease-in-out infinite;
}

.sound-waves.is-active .wave:nth-child(1) { animation-delay: 0s; }
.sound-waves.is-active .wave:nth-child(2) { animation-delay: 0.1s; }
.sound-waves.is-active .wave:nth-child(3) { animation-delay: 0.2s; }
.sound-waves.is-active .wave:nth-child(4) { animation-delay: 0.3s; }
.sound-waves.is-active .wave:nth-child(5) { animation-delay: 0.4s; }

@keyframes waveAnim {
  0%, 100% { height: 4px; }
  50%      { height: 14px; }
}

/* ===== 提示 ===== */
.hint {
  margin-top: 8px;
  text-align: center;
  font-size: var(--font-size-xs);
  color: #c0a080;
}
</style>
