<template>
  <div class="home-container">
    <el-card class="home-card">
      <template #header>
        <div class="home-header">
          <h1>在线考试系统</h1>
          <div class="user-info">
            <span v-if="userStore.isLoggedIn">{{ userStore.user.userName }}</span>
            <el-button v-if="userStore.isLoggedIn" type="text" @click="handleLogout">退出登录</el-button>
            <template v-else>
              <el-button type="primary" @click="$router.push('/login')">登录</el-button>
              <el-button type="text" @click="$router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </template>
      <div class="home-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <h3>用户管理</h3>
                </div>
              </template>
              <div class="feature-content">
                <p>管理用户账号、个人信息</p>
                <el-button type="primary" @click="$router.push('/user/profile')">个人中心</el-button>
                <el-button v-if="userStore.userRole === 'admin'" type="success" @click="$router.push('/admin/user')">用户管理</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <h3>题目管理</h3>
                </div>
              </template>
              <div class="feature-content">
                <p>浏览、搜索、练习编程题和选择题</p>
                <el-button type="primary" @click="$router.push('/question/list')">题目列表</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <h3>考试管理</h3>
                </div>
              </template>
              <div class="feature-content">
                <p>参加考试、查看成绩</p>
                <el-button type="primary" @click="$router.push('/exam/list')">考试列表</el-button>
              </div>
            </el-card>
          </el-col>
        <el-col :span="8">
          <el-card class="feature-card">
            <template #header>
              <div class="feature-header">
                <h3>班级管理</h3>
              </div>
            </template>
            <div class="feature-content">
              <p>参加班级、查看班级</p>
              <el-button type="primary" @click="$router.push('/class/list')">班级列表</el-button>
            </div>
          </el-card>
        </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  // 初始化用户信息
  userStore.initUser()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.home-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.home-header h1 {
  color: #1890ff;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home-content {
  margin-top: 30px;
}

.feature-card {
  height: 200px;
  display: flex;
  flex-direction: column;
}

.feature-header {
  margin-bottom: 20px;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.feature-content p {
  margin-bottom: 20px;
  color: #606266;
}

.feature-content .el-button {
  margin-right: 10px;
}
</style>
