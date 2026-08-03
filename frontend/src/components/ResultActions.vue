<template>
  <!--
    ResultActions.vue — 结果页操作按钮栏
    2×2 网格布局：再念一次 / 查看全文 / 换一句 / 收藏
  -->
  <div class="result-actions">
    <button
      class="action-btn action-btn--play"
      :class="{ 'is-active': playing }"
      :disabled="switching"
      @click="$emit('toggle-play')"
    >
      <span class="btn-icon">{{ playing ? '⏸' : '🔊' }}</span>
      <span class="btn-label">{{ playing ? '暂停' : '再念一次' }}</span>
    </button>

    <button
      class="action-btn action-btn--full"
      @click="$emit('show-full')"
    >
      <span class="btn-icon">📜</span>
      <span class="btn-label">查看全文</span>
    </button>

    <button
      class="action-btn action-btn--switch"
      :disabled="switching"
      @click="$emit('switch-poem')"
    >
      <span class="btn-icon" :class="{ 'is-spinning': switching }">🎲</span>
      <span class="btn-label">{{ switching ? '换句中...' : '换一句' }}</span>
    </button>

    <button
      class="action-btn action-btn--fav"
      :class="{ 'is-favorited': favorited }"
      @click="$emit('toggle-fav')"
    >
      <span class="btn-icon">{{ favorited ? '❤️' : '🤍' }}</span>
      <span class="btn-label">{{ favorited ? '已收藏' : '收藏' }}</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  switching: { type: Boolean, default: false },
  favorited: { type: Boolean, default: false },
  playing: { type: Boolean, default: false }
})

defineEmits(['toggle-play', 'show-full', 'switch-poem', 'toggle-fav'])
</script>

<style scoped>
.result-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 0 var(--padding-page);
  margin-top: 16px;
}

/* ===== 按钮基础样式 ===== */
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px 12px;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.2s;
  font-family: inherit;
}

.action-btn:active {
  transform: scale(0.96);
}

.action-btn:disabled {
  opacity: 0.6;
  pointer-events: none;
}

.btn-icon {
  font-size: 26px;
  transition: transform 0.3s;
}

.btn-icon.is-spinning {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.btn-label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  letter-spacing: 1px;
}

/* ===== 各按钮配色 ===== */

/* 再念一次 — 暖橙色 */
.action-btn--play {
  background: rgba(232, 164, 64, 0.12);
  color: #b8751a;
}

.action-btn--play.is-active {
  background: rgba(232, 164, 64, 0.22);
  color: #9a6010;
}

/* 查看全文 — 暖棕色 */
.action-btn--full {
  background: rgba(140, 110, 80, 0.1);
  color: #6b5040;
}

/* 换一句 — 暖绿色 */
.action-btn--switch {
  background: rgba(120, 160, 100, 0.12);
  color: #5a7a3a;
}

/* 收藏 — 暖红色/粉色 */
.action-btn--fav {
  background: rgba(220, 140, 140, 0.12);
  color: #b86868;
}

.action-btn--fav.is-favorited {
  background: rgba(220, 100, 100, 0.16);
  color: #c04848;
}
</style>
