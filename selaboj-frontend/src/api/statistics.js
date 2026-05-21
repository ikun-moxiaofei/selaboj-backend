import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    if (response.data.code === 0) {
      return response.data
    } else {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

const statisticsApi = {
  getStatistics: () => {
    return api.get('/statistics/get')
  }
}

export default statisticsApi