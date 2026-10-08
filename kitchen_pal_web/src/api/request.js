import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  // 本地 Ollama 大模型单次生成需 30~60s，15s 会必然超时；统一放宽到 120s。
  // 非 AI 接口响应很快，放宽不影响体验，只是失败时多等一会儿。
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：注入 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理 ApiResponse 结构
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一返回 { code, message, data }
    if (res.code !== undefined && res.code !== 200) {
      console.error(`[API Error] ${res.message}`)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 返回 data 字段（剥离外层包装）
    return res.data !== undefined ? res.data : res
  },
  (error) => {
    console.error(`[Network Error] ${error.message}`)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_logged_in')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request
