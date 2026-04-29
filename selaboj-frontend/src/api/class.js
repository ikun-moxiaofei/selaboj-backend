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

const classApi = {
  // 创建班级
  addClass: (data) => {
    return api.post('/class/add', data)
  },
  // 删除班级
  deleteClass: (data) => {
    return api.post('/class/delete', data)
  },
  // 更新班级
  updateClass: (data) => {
    return api.post('/class/update', data)
  },
  // 获取班级详情
  getClass: (id) => {
    return api.get(`/class/get?id=${id}`)
  },
  // 获取班级详情（脱敏）
  getClassVO: (id) => {
    return api.get(`/class/get/vo?id=${id}`)
  },
  // 分页获取班级列表
  listClassVOByPage: (data) => {
    return api.post('/class/list/page/vo', data)
  },
  // 添加班级成员
  addClassMembers: (data) => {
    return api.post('/class/member/add', data)
  },
  // 移除班级成员
  removeClassMembers: (data) => {
    return api.post('/class/member/remove', data)
  },
  // 获取班级成员列表
  getClassMembers: (classId) => {
    return api.get(`/class/member/list?classId=${classId}`)
  },
  // 学生加入班级
  joinClass: (classCode) => {
    return api.post(`/class/join?classCode=${classCode}`)
  },
  // 学生退出班级
  leaveClass: (classId) => {
    return api.post(`/class/leave?classId=${classId}`)
  },
  // 获取学生加入的班级列表
  getMyClasses: () => {
    return api.get('/class/my/list')
  }
}

export default classApi
