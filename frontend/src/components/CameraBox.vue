<template>
  <!--
    CameraBox.vue — 相机取景与拍照组件

    双模式自适应：
    1. HTTPS / localhost → WebRTC 实时取景画面
    2. HTTP 局域网     → 直接调起系统相机（无需权限）

    拍照产出：{ base64, file, previewUrl }
  -->
  <div class="camera-box">
    <!-- ===== 取景区域 ===== -->
    <div class="viewfinder" @click="handleViewfinderClick">
      <!-- WebRTC 实时视频流 -->
      <video
        v-show="mode === 'webrtc' && streamReady"
        ref="videoEl"
        class="video-stream"
        autoplay
        playsinline
        muted
      />

      <!-- Fallback: 已选照片预览 -->
      <img
        v-if="mode === 'fallback' && previewUrl"
        :src="previewUrl"
        alt="拍摄预览"
        class="preview-image"
      />

      <!-- Fallback: 未拍照时显示引导（HTTP 模式） -->
      <div v-if="mode === 'fallback' && !previewUrl" class="fallback-guide">
        <div class="guide-icon">📷</div>
        <p class="guide-text">点击屏幕或下方按钮拍照</p>
        <p class="guide-sub">将自动打开手机相机</p>
      </div>

      <!-- Fallback: 透明相机 input 覆盖取景区域 -->
      <input
        v-if="mode === 'fallback' && !previewUrl"
        ref="viewfinderCameraInput"
        type="file"
        accept="image/*"
        capture="environment"
        class="camera-input-overlay"
        @change="handleFallbackFile"
      />

      <!-- WebRTC 等待中 -->
      <div v-if="mode === 'webrtc' && !streamReady" class="viewfinder-placeholder">
        <div class="placeholder-icon">📷</div>
        <p v-if="!permissionDenied" class="placeholder-text">正在启动相机...</p>
        <template v-else>
          <p class="placeholder-text">无法启动相机</p>
          <van-button round size="small" type="primary" @click.stop="initCamera">
            重新授权
          </van-button>
        </template>
      </div>

      <!-- 取景框装饰 -->
      <div class="frame-corners">
        <span class="corner tl" />
        <span class="corner tr" />
        <span class="corner bl" />
        <span class="corner br" />
      </div>

      <!-- WebRTC 画面引导 -->
      <div class="viewfinder-hint" v-if="streamReady">
        <span>将物体放在框内</span>
      </div>
    </div>

    <!-- ===== 底部操作栏 ===== -->
    <div class="camera-bar">
      <div class="side-action" @click="triggerAlbum">
        <div class="side-icon">🖼️</div>
        <span class="side-label">相册</span>
      </div>

      <!-- 快门按钮 -->
      <div class="shutter-wrapper" @click="capture">
        <div class="shutter-outer">
          <div class="shutter-inner" />
        </div>
      </div>

      <div class="side-action side-action--ghost">
        <div class="side-icon" />
        <span class="side-label" />
      </div>
    </div>

    <!-- 相册选择 -->
    <input
      ref="albumInput"
      type="file"
      accept="image/*"
      class="hidden-input"
      @change="handleFileFromAlbum"
    />

    <!-- Fallback 确认弹窗 -->
    <van-action-sheet
      v-model:show="showPreviewSheet"
      title="确认照片"
      :actions="[{ name: 'confirm', subname: '使用此照片开始识别', color: '#e8a440' }]"
      cancel-text="返回重拍"
      @select="confirmFallbackPhoto"
      @cancel="cancelFallbackPhoto"
    />

    <!-- 闪白动画 -->
    <transition name="flash">
      <div v-if="flashVisible" class="flash-overlay" />
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { showToast } from 'vant'

const emit = defineEmits(['captured', 'error'])

// ===== 状态 =====
const mode = ref('webrtc')
const streamReady = ref(false)
const permissionDenied = ref(false)
const videoEl = ref(null)
const viewfinderCameraInput = ref(null)
const albumInput = ref(null)
const previewUrl = ref('')
const pendingData = ref(null)
const showPreviewSheet = ref(false)
const flashVisible = ref(false)

let mediaStream = null
const MAX_WIDTH = 1024

// ===== 生命周期 =====
onMounted(() => initCamera())
onUnmounted(() => stopStream())

// ===== 模式检测 =====

/** 是否需要跳过 WebRTC 直接进入降级模式 */
function shouldSkipWebRTC() {
  // 1. 不是安全上下文（HTTP，非 localhost）
  if (!window.isSecureContext && window.location.hostname !== 'localhost') {
    return true
  }
  // 2. 不支持 getUserMedia
  if (!navigator.mediaDevices?.getUserMedia) {
    return true
  }
  return false
}

// ===== 相机初始化 =====

async function initCamera() {
  permissionDenied.value = false

  // HTTP 环境直接降级，不浪费时间尝试 WebRTC
  if (shouldSkipWebRTC()) {
    switchToFallback()
    return
  }

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 1280 } },
      audio: false
    })
    if (videoEl.value) videoEl.value.srcObject = mediaStream
    mode.value = 'webrtc'
    streamReady.value = true
  } catch (err) {
    permissionDenied.value = true
    switchToFallback()
  }
}

function stopStream() {
  if (mediaStream) {
    mediaStream.getTracks().forEach((t) => t.stop())
    mediaStream = null
  }
  streamReady.value = false
}

function switchToFallback() {
  stopStream()
  mode.value = 'fallback'
}

// ===== 取景区域点击 =====

/** 点击取景框 → fallback 模式下触发拍照（作为 input 覆盖层的兜底） */
function handleViewfinderClick() {
  // 当 input 覆盖层未渲染时（如已选了照片），不做任何事
  if (mode.value === 'webrtc' && !streamReady.value) {
    // WebRTC 未就绪：也是降级拍照
    capture()
  }
}

// ===== 拍照 =====

function capture() {
  if (mode.value === 'webrtc' && streamReady.value) {
    captureFromVideo()
  } else {
    // Fallback: 触发覆盖在取景框上的 file input
    viewfinderCameraInput.value?.click()
  }
}

/** WebRTC：从 video 截取当前帧 */
function captureFromVideo() {
  const video = videoEl.value
  if (!video) return

  flashVisible.value = true
  setTimeout(() => { flashVisible.value = false }, 300)

  const canvas = document.createElement('canvas')
  canvas.width = video.videoWidth || 640
  canvas.height = video.videoHeight || 480
  const ctx = canvas.getContext('2d')
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

  canvas.toBlob(
    (blob) => {
      if (!blob) return
      const file = new File([blob], `capture_${Date.now()}.jpg`, { type: 'image/jpeg' })
      const previewUrl = URL.createObjectURL(blob)
      const base64 = canvas.toDataURL('image/jpeg', 0.85)
      emit('captured', { base64, file, previewUrl })
    },
    'image/jpeg',
    0.85
  )
}

// ===== 相册 =====

function triggerAlbum() {
  albumInput.value?.click()
}

/** 相册选图 → 不需要 capture 触发，直接处理 */
function handleFileFromAlbum(e) {
  processFileInput(e, albumInput)
}

// ===== Fallback：系统相机选图 =====

function handleFallbackFile(e) {
  processFileInput(e, viewfinderCameraInput)
}

function processFileInput(e, inputRef) {
  const file = e.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    return
  }

  // 先显示预览
  previewUrl.value = URL.createObjectURL(file)

  // 压缩处理
  compressAndConvert(file)
    .then((data) => {
      pendingData.value = data
      showPreviewSheet.value = true
    })
    .catch((err) => emit('error', err))

  // 清空以支持重复选择
  if (inputRef?.value) inputRef.value.value = ''
  e.target.value = ''
}

function confirmFallbackPhoto() {
  showPreviewSheet.value = false
  if (!pendingData.value) return

  const { base64, blob } = pendingData.value
  const file = new File([blob], `album_${Date.now()}.jpg`, { type: 'image/jpeg' })
  const previewUrl = URL.createObjectURL(blob)

  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)

  emit('captured', { base64, file, previewUrl })
}

function cancelFallbackPhoto() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  pendingData.value = null
  showPreviewSheet.value = false
}

// ===== 图片压缩 =====

function compressAndConvert(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let w = img.width, h = img.height
        if (w > MAX_WIDTH) { h = Math.round((h * MAX_WIDTH) / w); w = MAX_WIDTH }
        const canvas = document.createElement('canvas')
        canvas.width = w; canvas.height = h
        canvas.getContext('2d').drawImage(img, 0, 0, w, h)

        const base64 = canvas.toDataURL('image/jpeg', 0.8)
        canvas.toBlob(
          (blob) => blob ? resolve({ base64, blob }) : reject(new Error('处理失败')),
          'image/jpeg', 0.8
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}
</script>

<style scoped>
/* ===== 容器 ===== */
.camera-box { flex: 1; display: flex; flex-direction: column; }

/* ===== 取景框 ===== */
.viewfinder {
  flex: 1; position: relative; margin: 20px 20px 8px;
  border-radius: 20px; overflow: hidden; background: #2a2520;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15), inset 0 0 60px rgba(0,0,0,0.1);
}
.video-stream, .preview-image { width: 100%; height: 100%; object-fit: cover; }

/* Fallback 引导 */
.fallback-guide {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 8px;
  background: linear-gradient(180deg, #3a3530 0%, #2a2520 100%);
}
.guide-icon { font-size: 56px; opacity: 0.8; }
.guide-text { color: rgba(255,255,255,0.75); font-size: var(--font-size-md); }
.guide-sub { color: rgba(255,255,255,0.4); font-size: var(--font-size-xs); margin-top: 2px; }

/* WebRTC 占位 */
.viewfinder-placeholder {
  position: absolute; inset: 0; display: flex;
  flex-direction: column; align-items: center; justify-content: center;
  gap: 12px; background: #2a2520;
}
.placeholder-icon { font-size: 56px; opacity: 0.6; }
.placeholder-text { color: rgba(255,255,255,0.6); font-size: var(--font-size-sm); }

/* 四角装饰 */
.frame-corners { position: absolute; inset: 12px; pointer-events: none; }
.corner { position: absolute; width: 24px; height: 24px; border-color: rgba(255,255,255,0.4); border-style: solid; }
.corner.tl { top:0; left:0; border-width:3px 0 0 3px; border-radius:6px 0 0 0; }
.corner.tr { top:0; right:0; border-width:3px 3px 0 0; border-radius:0 6px 0 0; }
.corner.bl { bottom:0; left:0; border-width:0 0 3px 3px; border-radius:0 0 0 6px; }
.corner.br { bottom:0; right:0; border-width:0 3px 3px 0; border-radius:0 0 6px 0; }

.viewfinder-hint {
  position: absolute; bottom:20px; left:50%; transform:translateX(-50%);
  background:rgba(0,0,0,0.45); backdrop-filter:blur(6px);
  padding:6px 16px; border-radius:20px;
}
.viewfinder-hint span { color:rgba(255,255,255,0.75); font-size:var(--font-size-xs); letter-spacing:1px; }

/* ===== 操作栏 ===== */
.camera-bar {
  display:flex; align-items:center; justify-content:space-between;
  padding:12px 32px; padding-bottom:calc(12px + var(--safe-area-bottom));
}
.side-action {
  display:flex; flex-direction:column; align-items:center; gap:4px;
  cursor:pointer; -webkit-tap-highlight-color:transparent; user-select:none; width:64px;
}
.side-action:active { opacity:0.7; }
.side-action--ghost { visibility:hidden; }
.side-icon { font-size:28px; }
.side-label { font-size:var(--font-size-xs); color:var(--color-text-secondary); }

/* 快门 */
.shutter-wrapper { position:relative; cursor:pointer; -webkit-tap-highlight-color:transparent; user-select:none; }
.shutter-outer {
  width:72px; height:72px; border-radius:50%; border:4px solid var(--color-primary);
  display:flex; align-items:center; justify-content:center;
  transition:transform 0.15s; background:transparent;
}
.shutter-outer:active { transform:scale(0.9); }
.shutter-inner { width:54px; height:54px; border-radius:50%; background:var(--color-primary); }

/* 闪白 */
.flash-overlay { position:absolute; inset:0; background:#fff; pointer-events:none; z-index:10; }
.flash-enter-active { transition:opacity 0.15s ease; }
.flash-leave-active { transition:opacity 0.25s ease; }
.flash-enter-from, .flash-leave-to { opacity:0; }

/* 杂项 */
.hidden-input { display: none; }

/* 相机 input 透明覆盖层 — 用户手指直接触摸触发原生相机 */
.camera-input-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  opacity: 0;
  z-index: 5;
  /* 确保移动端可触摸 */
  font-size: 100px;
  cursor: pointer;
}
</style>
