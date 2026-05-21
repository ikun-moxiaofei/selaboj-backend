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

const teacherUserApi = {
  listStudents: (current, pageSize, userName, userAccount) => {
    return api.get('/teacher/user/list', {
      params: { current, pageSize, userName, userAccount }
    })
  },
  getStudent: (id) => {
    return api.get(`/teacher/user/get/${id}`)
  },
  updateStudent: (user) => {
    return api.post('/teacher/user/update', user)
  },
  deleteStudent: (id) => {
    return api.post('/teacher/user/delete', { id })
  },
  addStudent: (user) => {
    return api.post('/teacher/user/add', user)
  }
}

export default teacherUserApi