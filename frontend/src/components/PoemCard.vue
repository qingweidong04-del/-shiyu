<template>
  <!--
    PoemCard.vue — 诗歌卡片
    绘本风格展示：诗句（大字）、拼音、出处、白话解释
  -->
  <div class="poem-card">
    <!-- 顶部装饰 -->
    <div class="card-ornament">
      <span class="ornament-dot" />
      <span class="ornament-star">✦</span>
      <span class="ornament-dot" />
    </div>

    <!-- 诗句正文 -->
    <p class="poem-line">{{ result.poemLine }}</p>

    <!-- 拼音 -->
    <p class="poem-pinyin">{{ result.poemPinyin }}</p>

    <!-- 出处 -->
    <p class="poem-source">—— {{ result.poemSource }}</p>

    <!-- 装饰分割线 -->
    <div class="divider">
      <span class="divider-leaf">🍃</span>
    </div>

    <!-- 白话解释 -->
    <div class="explanation-section">
      <p class="explanation-label">💡 白话解释</p>
      <p class="explanation-text">{{ result.poemExplanation }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  result: { type: Object, required: true }
})
</script>

<style scoped>
.poem-card {
  margin: 0 var(--padding-page);
  padding: 20px var(--padding-card) 22px;
  /* 绘本纸张质感 */
  background: linear-gradient(180deg, #fffef9 0%, #fef9f0 100%);
  border-radius: var(--radius-lg);
  box-shadow:
    0 2px 16px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(200, 180, 150, 0.15);
  position: relative;
  overflow: hidden;
}

/* 顶部装饰 */
.card-ornament {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
}

.ornament-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-primary-light);
}

.ornament-star {
  font-size: 12px;
  color: var(--color-primary);
  opacity: 0.6;
}

/* 诗句 */
.poem-line {
  font-size: var(--font-size-poem);
  font-weight: 700;
  color: #4a3520;
  line-height: 1.7;
  letter-spacing: 3px;
  text-align: center;
  margin: 0;
}

/* 拼音 */
.poem-pinyin {
  margin-top: 10px;
  font-size: var(--font-size-xs);
  color: #a09080;
  text-align: center;
  letter-spacing: 1px;
  line-height: 1.6;
}

/* 出处 */
.poem-source {
  margin-top: 8px;
  font-size: var(--font-size-sm);
  color: #b8a48c;
  text-align: center;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 14px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e8dcc8, transparent);
  max-width: 60px;
}

.divider-leaf {
  font-size: 14px;
  margin: 0 8px;
  opacity: 0.5;
}

/* 解释 */
.explanation-label {
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: #a09080;
  margin-bottom: 6px;
}

.explanation-text {
  font-size: var(--font-size-sm);
  color: #6b5a48;
  line-height: 1.8;
  margin: 0;
}
</style>
