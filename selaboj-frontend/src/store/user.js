import { defineStore } from 'pinia'
import userApi from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    loading: false,
    error: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.user,
    userRole: (state) => state.user?.userRole || 'user'
  },
  actions: {
    async login(userAccount, userPassword) {
      this.loading = true
      this.error = null
      try {
        const response = await userApi.login({ userAccount, userPassword })
        this.user = response.data
        localStorage.setItem('user', JSON.stringify(response.data))
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '登录失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    async register(userAccount, userPassword, checkPassword, userRole) {
      this.loading = true
      this.error = null
      try {
        await userApi.register({ userAccount, userPassword, checkPassword, userRole })
      } catch (error) {
        this.error = error.response?.data?.message || '注册失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    async logout() {
      try {
        await userApi.logout()
      } catch (error) {
        console.error('登出失败', error)
      } finally {
        this.user = null
        localStorage.removeItem('user')
      }
    },
    async getCurrentUser() {
      this.loading = true
      this.error = null
      try {
        const response = await userApi.getCurrentUser()
        this.user = response.data
        localStorage.setItem('user', JSON.stringify(response.data))
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '获取用户信息失败'
        this.user = null
        localStorage.removeItem('user')
        throw error
      } finally {
        this.loading = false
      }
    },
    async updateProfile(userUpdateMyRequest) {
      this.loading = true
      this.error = null
      try {
        await userApi.updateProfile(userUpdateMyRequest)
        // 重新获取用户信息
        await this.getCurrentUser()
      } catch (error) {
        this.error = error.response?.data?.message || '更新个人信息失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    initUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          this.user = JSON.parse(userStr)
        } catch (error) {
          console.error('解析用户信息失败', error)
          localStorage.removeItem('user')
          this.user = null
        }
      }
    }
  }
})
