<template>
  <!--
    Discover.vue — 历史发现页
    时间线展示所有保存的发现记录，支持滑动删除
    MVP：localStorage 持久化，Pinia 管理状态
  -->
  <div class="discover-page">
    <!-- 头部 -->
    <van-nav-bar
      title="我的发现"
      left-text="返回"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />

    <!-- ===== 空状态 ===== -->
    <div v-if="store.historyList.length === 0" class="empty-area">
      <div class="empty-illustration">📖</div>
      <p class="empty-title">还没有发现记录</p>
      <p class="empty-desc">拍下身边的自然景物，发现古诗之美</p>
      <van-button type="primary" round @click="goCamera">
        去拍照发现
      </van-button>
    </div>

    <!-- ===== 时间线 ===== -->
    <div v-else class="timeline">
      <!-- 时间线竖线 -->
      <div class="timeline-line" />

      <van-swipe-cell
        v-for="item in store.historyList"
        :key="item.id"
        :right-width="72"
      >
        <!-- 时间线卡片 -->
        <div class="timeline-item" @click="goDetail(item)">
          <!-- 时间节点 -->
          <div class="timeline-dot" />

          <!-- 日期标签 -->
          <p class="timeline-date">{{ formatDate(item.createdAt) }}</p>

          <!-- 卡片内容 -->
          <div class="timeline-card">
            <div class="card-thumb">
              <img
                v-if="item.photoBase64"
                :src="item.photoBase64"
                alt="照片"
                class="thumb-img"
                @error="(e) => (e.target.style.display = 'none')"
              />
              <span v-else class="thumb-placeholder">🖼️</span>
            </div>

            <div class="card-body">
              <p class="card-poem">{{ item.poemLine }}</p>
              <p class="card-source">{{ item.poemSource || (item.poemAuthor ? item.poemAuthor + ' 作' : '') }}</p>
              <p class="card-time">{{ formatTime(item.createdAt) }}</p>
            </div>

            <van-icon name="arrow" class="card-arrow" />
          </div>
        </div>

        <!-- 滑动删除按钮 -->
        <template #right>
          <div class="swipe-delete" @click="handleDelete(item.id)">
            <van-icon name="delete-o" size="20px" />
            <span>删除</span>
          </div>
        </template>
      </van-swipe-cell>

      <!-- 底部提示 -->
      <p class="timeline-end">— 共 {{ store.historyCount }} 次发现 —</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useDiscoveryStore } from '../store/discovery.js'
import { showToast, showDialog } from 'vant'

const router = useRouter()
const store = useDiscoveryStore()

// ===== 导航 =====

function goDetail(item) {
  // 将历史记录加载到 currentResult，Result 页可正常展示
  store.currentResult = {
    ...item,
    saved: true,
    // 兼容 Result 页 photoSrc 取值
    photoUrl: item.photoBase64 || ''
  }
  // 注意：currentImage 不设置，Result 页会回退到 photoUrl
  router.push({ name: 'Result', params: { id: item.id } })
}

function goCamera() {
  router.push({ name: 'Camera' })
}

function goBack() {
  router.push({ name: 'Camera' })
}

// ===== 删除 =====

function handleDelete(id) {
  showDialog.confirm({
    title: '确认删除',
    message: '删除后无法恢复',
    confirmButtonColor: '#c04848'
  })
    .then(() => {
      store.deleteRecord(id)
      showToast('已删除')
    })
    .catch(() => {})
}

// ===== 时间格式化 =====

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')

  // 今天
  if (d.toDateString() === now.toDateString()) {
    return '今天'
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 今年内
  if (d.getFullYear() === now.getFullYear()) {
    return `${pad(d.getMonth() + 1)}月${pad(d.getDate())}日`
  }

  return `${d.getFullYear()}年${pad(d.getMonth() + 1)}月${pad(d.getDate())}日`
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.discover-page {
  min-height: 100%;
  background: var(--color-bg);
  padding-bottom: calc(24px + var(--safe-area-bottom));
}

/* ===== 空状态 ===== */
.empty-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  gap: 8px;
}

.empty-illustration {
  font-size: 64px;
  margin-bottom: 8px;
}

.empty-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.empty-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: 16px;
}

/* ===== 时间线容器 ===== */
.timeline {
  position: relative;
  padding: 16px var(--padding-page) 0;
}

/* 竖线 */
.timeline-line {
  position: absolute;
  left: 28px;
  top: 8px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg,
    var(--color-primary-light) 0%,
    #f0e8d0 100%);
}

/* ===== 时间线条目 ===== */
.timeline-item {
  position: relative;
  padding-left: 44px;
  margin-bottom: 4px;
  cursor: pointer;
}

/* 时间节点圆点 */
.timeline-dot {
  position: absolute;
  left: 21px;
  top: 20px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-primary);
  border: 2px solid #fef9ed;
  box-shadow: 0 0 0 3px var(--color-primary-light);
  z-index: 1;
}

/* 日期标签 */
.timeline-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  padding-top: 16px;
}

/* ===== 卡片 ===== */
.timeline-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  transition: background 0.15s;
}

.timeline-card:active {
  background: #fdf8f0;
}

.card-thumb {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  font-size: 24px;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-poem {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-source {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  margin-top: 3px;
}

.card-time {
  font-size: 11px;
  color: var(--color-text-light);
  margin-top: 2px;
}

.card-arrow {
  color: var(--color-text-light);
  flex-shrink: 0;
  font-size: 14px;
}

/* ===== 滑动删除按钮 ===== */
.swipe-delete {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 72px;
  background: #e88a88;
  color: #fff;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  font-size: var(--font-size-xs);
  cursor: pointer;
}

/* ===== 底部 ===== */
.timeline-end {
  text-align: center;
  padding: 24px 0;
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
}
</style>
