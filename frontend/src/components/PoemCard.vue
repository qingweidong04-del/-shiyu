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
  padding: 24px var(--padding-card) 22px;
  /* 绘本纸张质感 + 柔和光斑 */
  background:
    radial-gradient(circle at 12% 18%, rgba(232, 164, 64, 0.07) 0, transparent 42%),
    radial-gradient(circle at 88% 85%, rgba(180, 140, 90, 0.06) 0, transparent 46%),
    linear-gradient(180deg, #fffef9 0%, #fef9f0 100%);
  border-radius: var(--radius-lg);
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.06),
    0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(200, 180, 150, 0.18);
  position: relative;
  overflow: hidden;
  /* 卡片入场动画 */
  animation: cardReveal 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* 顶部彩色丝带装饰 */
.poem-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, var(--color-primary-light), var(--color-primary), var(--color-primary-light));
  opacity: 0.75;
}

/* 顶部装饰 */
.card-ornament {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
  animation: fadeUp 0.5s ease 0.05s both;
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

/* ===== 卡片内容逐层浮现（诗词出现动画）===== */
.poem-line     { animation: fadeUp 0.5s ease 0.12s both; }
.poem-pinyin   { animation: fadeUp 0.5s ease 0.26s both; }
.poem-source   { animation: fadeUp 0.5s ease 0.36s both; }
.divider       { animation: fadeUp 0.5s ease 0.46s both; }
.explanation-section { animation: fadeUp 0.5s ease 0.56s both; }

@keyframes cardReveal {
  from { opacity: 0; transform: translateY(26px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
