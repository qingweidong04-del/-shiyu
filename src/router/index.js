import { createRouter, createWebHashHistory } from 'vue-router'

/*
 * 路由配置
 * 使用 Hash 模式：兼容静态部署，无需服务端配置
 *
 * 页面路径：
 *   /             拍照页（首页）
 *   /result/:id   发现详情页
 *   /discover     历史发现列表
 *   /setting      设置页
 */

const routes = [
  {
    path: '/',
    name: 'Camera',
    component: () => import('../views/Camera.vue'),
    meta: { title: '拍遇存' }
  },
  {
    path: '/result/:id',
    name: 'Result',
    component: () => import('../views/Result.vue'),
    meta: { title: '发现详情' }
  },
  {
    path: '/discover',
    name: 'Discover',
    component: () => import('../views/Discover.vue'),
    meta: { title: '历史发现' }
  },
  {
    path: '/setting',
    name: 'Setting',
    component: () => import('../views/Setting.vue'),
    meta: { title: '设置' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：更新页面标题
router.afterEach((to) => {
  document.title = to.meta.title || '拍遇存'
})

export default router
