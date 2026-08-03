# 📖 拍遇存

> AI 拍照发现古诗的儿童诗词启蒙应用

拍下一片荷叶，遇见一首"小荷才露尖尖角"。用 AI 的眼睛发现身边的美好，用古人的诗句感受自然的魅力。

---

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| 📷 **拍照识别** | 拍摄自然景物，AI 自动识别物体（花、鸟、山、水……） |
| 📝 **古诗匹配** | 根据识别结果匹配经典古诗，展示诗句、拼音、出处 |
| 🎙 **语音朗读** | 浏览器内置 TTS 朗读诗句，语速适中适合儿童 |
| 💾 **发现记录** | 自动保存每次发现，时间线浏览 |
| 📤 **分享卡片** | 生成精美分享图片，保存到相册或分享给朋友 |
| ❤️ **收藏** | 收藏喜欢的诗句，随时回顾 |

---

## 🛠 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3 + Composition API | `<script setup>` 语法 |
| 构建 | Vite | 极速 HMR |
| UI | Vant 4 | 移动端组件库，按需引入 |
| 路由 | Vue Router 4 | Hash 模式，兼容静态部署 |
| 状态 | Pinia | 轻量状态管理 + localStorage 持久化 |
| HTTP | Axios | 封装拦截器，预留真实后端接口 |
| 语音 | SpeechSynthesis API | 浏览器内置，零成本 TTS |
| 分享 | Canvas API | 前端合成分享卡片 |

---

## 📂 项目结构

```
paiyucun/
├── public/
│   └── favicon.svg                  # 品牌图标
├── src/
│   ├── main.js                      # 入口：注册 Pinia、Router、Vant
│   ├── App.vue                      # 根组件
│   ├── style.css                    # 全局样式 + CSS 变量
│   │
│   ├── router/
│   │   └── index.js                 # 4 条路由（Camera / Result / Discover / Setting）
│   │
│   ├── store/
│   │   └── discovery.js             # Pinia Store：拍照 → 识别 → 保存 → 历史
│   │
│   ├── api/
│   │   ├── index.js                 # API 统一入口（自动切换 Mock / 真实后端）
│   │   ├── http.js                  # Axios 实例 + 拦截器
│   │   └── mock.js                  # Mock 实现
│   │
│   ├── mock/
│   │   └── poem.js                  # 诗词数据库（10 首） + AI 匹配逻辑
│   │
│   ├── composables/
│   │   └── useShareImage.js         # Canvas 分享图生成
│   │
│   ├── views/
│   │   ├── Camera.vue               # 首页：拍照
│   │   ├── Result.vue               # 核心页：古诗展示 + 朗读 + 操作
│   │   ├── Discover.vue             # 历史页：时间线 + 滑动删除
│   │   └── Setting.vue              # 设置页
│   │
│   └── components/
│       ├── CameraBox.vue            # 相机取景（WebRTC / 系统相机双模式）
│       ├── PoemCard.vue             # 诗歌卡片（诗句 + 拼音 + 释义）
│       ├── AudioPlayer.vue          # 语音朗读播放器
│       ├── ResultActions.vue        # 操作按钮栏
│       ├── FullPoemSheet.vue        # 全文弹窗
│       └── ShareCard.vue            # (预留) 分享卡片
│
├── .env.development                 # 开发环境（Mock 模式）
├── .env.production                  # 生产环境（真实 API）
├── vite.config.js                   # Vite 配置 + Vant 按需引入
└── index.html                       # 入口 HTML
```

---

## 🚀 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览构建产物
npm run preview
```

### 移动端测试

开发服务器默认绑定 `0.0.0.0`，手机与电脑连同一 Wi-Fi 即可访问：

```
http://<电脑IP>:5173
```

> HTTP 环境下会自动使用系统相机模式（`<input capture>`），无需 HTTPS 权限。

如需实时摄像头取景画面，使用 ngrok 创建 HTTPS 隧道：

```bash
npx ngrok http 5173
```

---

## 🔌 API 接口

### Mock 模式（默认）

开发阶段使用内置 Mock，无需后端即可运行完整流程。

```
VITE_USE_MOCK=true    # 使用 Mock（默认）
VITE_USE_MOCK=false   # 使用真实后端
```

### 真实后端接口约定

| 方法 | 路径 | 参数 | 返回 |
|------|------|------|------|
| POST | `/api/image/upload` | FormData `file` | `{ imageUrl, object }` |
| GET | `/api/poem/{object}` | path 参数 | 诗词完整数据 |
| GET | `/api/poem/random` | query `exclude` | 诗词完整数据 |
| POST | `/api/discoveries` | body 数据 | `{ success, id }` |
| GET | `/api/discoveries` | query `page, size` | `{ list, total }` |

切换到真实后端只需修改 `.env.production`：

```env
VITE_API_BASE_URL=https://api.paiyucun.com
VITE_USE_MOCK=false
```

---

## 🎯 用户流程

```
打开 App
  │
  ▼
┌──────────┐
│  拍照页   │ ← WebRTC 实时取景 / 系统相机
└────┬─────┘
     │ 拍照
     ▼
┌──────────┐
│  AI 识别  │ ← uploadImage → getPoem
└────┬─────┘
     │
     ▼
┌──────────┐
│  结果页   │ ← 照片 + 诗句 + 拼音 + 朗读 + 操作按钮
└────┬─────┘
     │
     ├── 🔊 再念一次  → SpeechSynthesis 朗读
     ├── 📜 查看全文  → 弹出完整诗文
     ├── 🎲 换一句    → 同场景另一首诗
     ├── ❤️ 收藏      → 加入收藏列表
     └── 📤 分享      → 生成分享卡片图片
     │
     ▼
┌──────────┐
│  历史页   │ ← 时间线展示 + 左滑删除
└──────────┘
```

---

## 🎨 设计风格

- **儿童绘本风**：暖米色渐变背景、柔和圆角、纸张质感卡片
- **暖金色主题**：主色 `#e8a440`，辅以暖棕、暖绿、暖粉
- **移动端优先**：`max-width: 480px` 居中布局，适配安全区域
- **CSS 变量**：统一管理颜色、字号、间距、圆角

---

## 📦 部署

```bash
npm run build
```

`dist/` 目录为纯静态文件，可部署到：

- **Vercel**：`vercel --prod`
- **Netlify**：拖拽 `dist/` 到面板
- **GitHub Pages**：配置 Actions 自动部署
- **Nginx**：将 `dist/` 设为静态资源目录

> ⚠️ SpeechSynthesis API 需要 HTTPS 或 localhost 才能使用。生产部署务必启用 HTTPS。

---

## 📄 License

MIT
