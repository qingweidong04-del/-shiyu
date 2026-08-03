import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // Vant 组件按需自动引入
    Components({
      resolvers: [VantResolver()]
    })
  ],
  // 开发服务器配置：局域网可访问，方便真机调试
  server: {
    host: '0.0.0.0',
    port: 5173
  }
})
