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

const examApi = {
  // 创建考试
  addExam: (data) => {
    return api.post('/exam/add', data)
  },
  // 删除考试
  deleteExam: (data) => {
    return api.post('/exam/delete', data)
  },
  // 更新考试
  updateExam: (data) => {
    return api.post('/exam/update', data)
  },
  // 获取考试详情
  getExam: (id) => {
    return api.get(`/exam/get?id=${id}`)
  },
  // 获取考试详情（脱敏）
  getExamVO: (id) => {
    return api.get(`/exam/get/vo?id=${id}`)
  },
  // 分页获取考试列表
  listExamVOByPage: (data) => {
    return api.post('/exam/list/page/vo', data)
  },
  // 发布考试到班级
  publishExamToClass: (examId, classId) => {
    return api.post(`/exam/publish?examId=${examId}&classId=${classId}`)
  },
  // 开始考试
  startExam: (examId) => {
    return api.post(`/exam/start?examId=${examId}`)
  },
  // 提交答案
  submitAnswer: (examRecordId, questionId, answer) => {
    return api.post(`/exam/submit?examRecordId=${examRecordId}&questionId=${questionId}&answer=${encodeURIComponent(answer)}`)
  },
  // 结束考试
  endExam: (examRecordId) => {
    return api.post(`/exam/end?examRecordId=${examRecordId}`)
  },
  // 获取考试记录
  getExamRecord: (examRecordId) => {
    return api.get(`/exam/record/get?examRecordId=${examRecordId}`)
  },
  // 获取用户考试记录列表
  getUserExamRecords: (examId) => {
    return api.get(`/exam/record/list?examId=${examId}`)
  },
  // 自动评分
  autoGrade: (examRecordId) => {
    return api.post(`/exam/grade?examRecordId=${examRecordId}`)
  }
}

export default examApi
