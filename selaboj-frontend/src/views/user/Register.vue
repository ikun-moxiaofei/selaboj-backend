<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>用户注册</h2>
        </div>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="80px">
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model="registerForm.userAccount" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword">
          <el-input v-model="registerForm.userPassword" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPassword">
          <el-input v-model="registerForm.checkPassword" type="password" placeholder="请确认密码" />
        </el-form-item>
        <el-form-item label="用户角色" prop="userRole">
          <el-select v-model="registerForm.userRole" placeholder="请选择用户角色">
            <el-option label="学生" value="user" />
            <el-option label="老师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="router.back()">登录</el-button>
        </el-form-item>
      </el-form>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)
const error = ref('')

const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userRole: 'user'
})

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.userPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''
      try {
        await userStore.register(
          registerForm.userAccount,
          registerForm.userPassword,
          registerForm.checkPassword,
          registerForm.userRole
        )
        ElMessage.success('注册成功')
        router.back()
      } catch (err) {
        error.value = userStore.error || '注册失败'
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.register-card {
  width: 400px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 20px;
}

.error-message {
  color: #f56c6c;
  text-align: center;
  margin-top: 10px;
}
</style>
