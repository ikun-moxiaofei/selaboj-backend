import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
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

const questionApi = {
  // 创建题目
  addQuestion: (data) => {
    return api.post('/question/add', data)
  },
  // 删除题目
  deleteQuestion: (data) => {
    return api.post('/question/delete', data)
  },
  // 更新题目
  updateQuestion: (data) => {
    return api.post('/question/update', data)
  },
  // 编辑题目
  editQuestion: (data) => {
    return api.post('/question/edit', data)
  },
  // 获取题目详情
  getQuestion: (id) => {
    return api.get(`/question/get?id=${id}`)
  },
  // 获取题目详情（脱敏）
  getQuestionVO: (id) => {
    return api.get(`/question/get/vo?id=${id}`)
  },
  // 分页获取题目列表
  listQuestionVOByPage: (data) => {
    return api.post('/question/list/page/vo', data)
  },
  // 分页获取当前用户创建的题目列表
  listMyQuestionVOByPage: (data) => {
    return api.post('/question/my/list/page/vo', data)
  }
}

export default questionApi
