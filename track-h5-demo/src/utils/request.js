import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default service
