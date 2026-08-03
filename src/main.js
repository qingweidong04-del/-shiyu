import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router/index.js'

// Vant 样式
import 'vant/lib/index.css'
// 全局样式
import './style.css'

import App from './App.vue'

const app = createApp(App)

// 状态管理
app.use(createPinia())
// 路由
app.use(router)

app.mount('#app')
