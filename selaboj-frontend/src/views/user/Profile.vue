<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人信息</h2>
        </div>
      </template>
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model="userForm.userAccount" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-input v-model="userForm.userRole" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdate" :loading="loading">更新信息</el-button>
          <el-button @click="handleLogout">退出登录</el-button>
        </el-form-item>
      </el-form>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const userFormRef = ref(null)
const loading = ref(false)
const error = ref('')

const userForm = reactive({
  userAccount: '',
  userName: '',
  userRole: ''
})

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

const handleUpdate = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''
      try {
        await userStore.updateProfile({
          userName: userForm.userName
        })
        // 刷新页面
        location.reload()
      } catch (err) {
        error.value = userStore.error || '更新失败'
      } finally {
        loading.value = false
      }
    }
  })
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

const loadUserInfo = async () => {
  try {
    const userInfo = await userStore.getCurrentUser()
    userForm.userAccount = userInfo.userAccount
    userForm.userName = userInfo.userName
    userForm.userRole = userInfo.userRole
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 0 20px;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-header {
  text-align: center;
  margin-bottom: 20px;
}

.error-message {
  color: #f56c6c;
  text-align: center;
  margin-top: 10px;
}
</style>
