<template>
  <!--
    Camera.vue — 首页 · 拍照页
    儿童绘本风格：温暖、自然、柔和
    打开 App 直接进入，展示实时相机取景
  -->
  <div class="camera-page">
    <!-- ===== 装饰背景 ===== -->
    <div class="bg-decor">
      <span class="decor-cloud cloud-1">☁️</span>
      <span class="decor-cloud cloud-2">☁️</span>
      <span class="decor-leaf leaf-1">🌿</span>
      <span class="decor-leaf leaf-2">🌸</span>
    </div>

    <!-- ===== 顶部区域 ===== -->
    <div class="header">
      <!-- 品牌名 -->
      <div class="brand">
        <span class="brand-icon">📖</span>
        <h1 class="brand-name">拍遇存</h1>
      </div>

      <!-- 右上角：我的发现 -->
      <div class="header-action" @click="goDiscover">
        <span class="action-icon">🌟</span>
        <span class="action-text">我的发现</span>
      </div>
    </div>

    <!-- ===== 实时相机取景 ===== -->
    <CameraBox
      @captured="handleCaptured"
      @error="handleCameraError"
    />

    <!-- ===== 底部引导语 ===== -->
    <div class="footer-hint">
      <p class="hint-text">拍下身边的花草树木、鸟兽虫鱼</p>
      <p class="hint-sub">AI 会为你找到一首古诗</p>
    </div>

    <!-- ===== 识别中的遮罩 ===== -->
    <van-overlay :show="store.isRecognizing" class="recognizing-overlay" :duration="0.3">
      <div class="recognizing-card">
        <!-- 旋转的小花 -->
        <div class="spinner-flower">🌸</div>
        <p class="recognizing-title">正在观察...</p>
        <p class="recognizing-sub">AI 正在欣赏你拍下的世界</p>
        <!-- 进度点动画 -->
        <div class="dot-bounce">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
      </div>
    </van-overlay>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useDiscoveryStore } from '../store/discovery.js'
import { showToast } from 'vant'
import CameraBox from '../components/CameraBox.vue'

const router = useRouter()
const store = useDiscoveryStore()

/**
 * 拍照完整流程：
 *   1. 图片数据写入 Pinia Store
 *   2. 发起 AI 识别（Mock）
 *   3. 跳转到结果页
 *
 * @param {{ base64: string, file: File|null, previewUrl: string }} payload
 */
async function handleCaptured(payload) {
  try {
    // ① 图片数据写入 Store（Result 页需要用预览地址展示照片）
    store.setImageData(payload)

    // ② 发起识别
    await store.discover(payload.base64)

    // ③ 自动保存到历史记录
    store.addRecord()

    // ④ 跳转结果页
    router.push({
      name: 'Result',
      params: { id: store.currentResult.id }
    })
  } catch (err) {
    showToast(err.message || '识别失败，请重试')
  }
}

/** 相机权限被拒等异常 */
function handleCameraError(err) {
  showToast(err.message || '相机启动失败，请检查权限')
}

/** 跳转历史发现页 */
function goDiscover() {
  router.push({ name: 'Discover' })
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.camera-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  /* 绘本风格的暖米色渐变背景 */
  background: linear-gradient(
    180deg,
    #fef9ed 0%,
    #fdf5e6 30%,
    #faf0dc 100%
  );
}

/* ===== 装饰元素 ===== */
.bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.decor-cloud {
  position: absolute;
  font-size: 42px;
  opacity: 0.3;
  animation: floatCloud 8s ease-in-out infinite;
}

.cloud-1 { top: 8%;  right: 12%; animation-delay: 0s; }
.cloud-2 { top: 18%; left: 8%;  animation-delay: 3s; font-size: 32px; }

.decor-leaf {
  position: absolute;
  font-size: 24px;
  opacity: 0.35;
  bottom: 18%;
}

.leaf-1 { right: 10%; animation: swayLeaf 6s ease-in-out infinite; }
.leaf-2 { left: 12%;  animation: swayLeaf 5s ease-in-out infinite 2s; }

@keyframes floatCloud {
  0%, 100% { transform: translateX(0); }
  50%      { transform: translateX(-18px); }
}

@keyframes swayLeaf {
  0%, 100% { transform: rotate(-3deg); }
  50%      { transform: rotate(3deg); }
}

/* ===== 顶部 ===== */
.header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  padding-top: calc(12px + env(safe-area-inset-top, 0px));
}

/* 品牌名 */
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  font-size: 24px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: #5c3d2e;
  letter-spacing: 2px;
  margin: 0;
}

/* 我的发现按钮 */
.header-action {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(232, 164, 64, 0.12);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  transition: background 0.2s;
}

.header-action:active {
  background: rgba(232, 164, 64, 0.22);
}

.action-icon {
  font-size: 16px;
}

.action-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary-dark);
}

/* ===== 底部引导语 ===== */
.footer-hint {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 14px 20px;
  padding-bottom: calc(14px + var(--safe-area-bottom));
}

.hint-text {
  font-size: var(--font-size-sm);
  color: #8b7355;
  letter-spacing: 1px;
  margin: 0;
}

.hint-sub {
  font-size: var(--font-size-xs);
  color: #b8a48c;
  margin: 4px 0 0;
}

/* ===== 识别中遮罩 ===== */
.recognizing-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(253, 250, 243, 0.92);
  z-index: 100;
}

.recognizing-card {
  text-align: center;
  padding: 40px 32px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}

.spinner-flower {
  font-size: 48px;
  animation: spinFlower 1.8s linear infinite;
}

@keyframes spinFlower {
  0%   { transform: rotate(0deg) scale(1); }
  50%  { transform: rotate(180deg) scale(1.15); }
  100% { transform: rotate(360deg) scale(1); }
}

.recognizing-title {
  margin-top: 16px;
  font-size: 20px;
  font-weight: 600;
  color: #5c3d2e;
}

.recognizing-sub {
  margin-top: 6px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* 三点弹跳 */
.dot-bounce {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}

.dot-bounce .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: bounce 1.2s ease-in-out infinite;
}

.dot-bounce .dot:nth-child(2) { animation-delay: 0.2s; }
.dot-bounce .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40%           { transform: translateY(-12px); }
}
</style>
