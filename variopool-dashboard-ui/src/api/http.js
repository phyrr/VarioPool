import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('variopool_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code !== 0) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(result)
    }
    return result.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('variopool_token')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http
