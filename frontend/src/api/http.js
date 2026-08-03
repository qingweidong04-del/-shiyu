/*
 * api/http.js — Axios 实例
 *
 * 真实后端请求的底层封装。
 * Mock 模式下不会被调用，仅在后端就绪后生效。
 */
import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    // TODO: 后续接入登录后添加 token
    // const token = localStorage.getItem('token')
    // if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一解包 { code, data, message }
http.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data ?? res
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    return Promise.reject(new Error(message))
  }
)

export default http
