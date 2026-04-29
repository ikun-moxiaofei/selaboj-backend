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

const userApi = {
  // 登录
  login: (data) => {
    return api.post('/user/login', data)
  },
  // 注册
  register: (data) => {
    return api.post('/user/register', data)
  },
  // 登出
  logout: () => {
    return api.post('/user/logout')
  },
  // 获取当前登录用户
  getCurrentUser: () => {
    return api.get('/user/get/login')
  },
  // 更新个人信息
  updateProfile: (data) => {
    return api.post('/user/update/my', data)
  },
  // 获取用户列表（分页）
  getUserList: (data) => {
    return api.post('/user/list/page/vo', data)
  },
  // 添加用户（管理员）
  addUser: (data) => {
    return api.post('/user/add', data)
  },
  // 更新用户（管理员）
  updateUser: (data) => {
    return api.post('/user/update', data)
  },
  // 删除用户（管理员）
  deleteUser: (data) => {
    return api.post('/user/delete', data)
  }
}

export default userApi
