<template>
  <!--
    FullPoemSheet.vue — 查看全文弹窗
    使用 Vant Popup 从底部弹出，展示完整诗文
  -->
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ maxHeight: '75%', borderRadius: '20px 20px 0 0' }"
    closeable
    close-icon-position="top-right"
  >
    <div class="sheet-content">
      <!-- 标题 -->
      <h3 class="sheet-title">{{ result.poemSource }}</h3>

      <!-- 装饰分割线 -->
      <div class="ornament-divider">
        <span class="ornament">🌸</span>
      </div>

      <!-- 完整诗句 -->
      <div class="full-poem">
        <p
          v-for="(line, idx) in poemLines"
          :key="idx"
          class="poem-verse"
        >{{ line }}</p>
      </div>

      <!-- 完整拼音 -->
      <div class="full-pinyin">
        <p
          v-for="(line, idx) in pinyinLines"
          :key="idx"
          class="pinyin-verse"
        >{{ line }}</p>
      </div>

      <!-- 装饰分割线 -->
      <div class="ornament-divider">
        <span class="ornament">📖</span>
      </div>

      <!-- 全文释义 -->
      <div class="full-explanation">
        <p class="explanation-label">💡 全文释义</p>
        <p class="explanation-text">{{ result.fullExplanation }}</p>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { computed } from 'vue'

const emit = defineEmits(['update:show'])

const props = defineProps({
  show: Boolean,
  result: { type: Object, required: true }
})

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

/** 按换行拆分诗句 */
const poemLines = computed(() => {
  return (props.result.fullPoem || '').split('\n').filter(Boolean)
})

const pinyinLines = computed(() => {
  return (props.result.fullPinyin || '').split('\n').filter(Boolean)
})
</script>

<style scoped>
.sheet-content {
  padding: 28px 24px;
  padding-bottom: calc(28px + var(--safe-area-bottom));
  background: linear-gradient(180deg, #fef9ed 0%, #fdf5e6 100%);
}

/* 标题 */
.sheet-title {
  text-align: center;
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #5c3d2e;
  margin: 0;
  letter-spacing: 1px;
}

/* 装饰分割线 */
.ornament-divider {
  text-align: center;
  margin: 16px 0;
}

.ornament {
  font-size: 18px;
  opacity: 0.5;
}

/* 诗句 */
.full-poem {
  text-align: center;
  margin: 8px 0;
}

.poem-verse {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 2;
  letter-spacing: 2px;
  margin: 0;
}

/* 拼音 */
.full-pinyin {
  text-align: center;
  margin-top: 12px;
}

.pinyin-verse {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.8;
  letter-spacing: 1px;
  margin: 0;
}

/* 释义 */
.full-explanation {
  margin-top: 4px;
}

.explanation-label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  text-align: center;
}

.explanation-text {
  font-size: var(--font-size-md);
  color: var(--color-text);
  line-height: 1.8;
  text-align: justify;
}
</style>
