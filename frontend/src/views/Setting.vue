<template>
  <!--
    Setting.vue — 设置页
    MVP 阶段为占位页面，后续可添加：
    - 清除缓存
    - 关于我们
    - 意见反馈
  -->
  <div class="setting-page page-container">
    <van-nav-bar title="设置" left-arrow @click-left="goBack" fixed placeholder />

    <van-cell-group inset>
      <van-cell title="清除缓存" is-link @click="handleClearCache" />
      <van-cell title="关于拍遇存" is-link @click="handleAbout" />
    </van-cell-group>

    <div class="version-info">
      <p>拍遇存 MVP v1.0.0</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useDiscoveryStore } from '../store/discovery.js'
import { showToast, showDialog } from 'vant'

const router = useRouter()
const store = useDiscoveryStore()

function goBack() {
  router.back()
}

function handleClearCache() {
  showDialog.confirm({
    title: '确认清除',
    message: '将清除所有历史发现记录（收藏不受影响）'
  }).then(() => {
    store.clearHistory()
    showToast('历史记录已清除')
  }).catch(() => {})
}

function handleAbout() {
  showDialog.alert({
    title: '关于拍遇存',
    message: '拍遇存 — AI拍照发现古诗\n\n用AI的眼睛发现身边的美好，用古人的诗句感受自然的魅力。\n\n让每个孩子都能在自然中发现古诗之美。'
  })
}
</script>

<style scoped>
.setting-page {
  background: #f7f8fa;
}

.version-info {
  text-align: center;
  margin-top: 40px;
  color: var(--color-text-light);
  font-size: var(--font-size-xs);
}
</style>
