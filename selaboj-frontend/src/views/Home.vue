<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">在线考试系统</h1>
        <p class="hero-subtitle">高效、便捷、专业的在线考试平台</p>
      </div>
      <div class="user-info">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-name">{{ userStore.user.userName }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" class="login-btn" @click="$router.push('/login')">登录</el-button>
          <el-button class="register-btn" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>

    <!-- 功能卡片区域 -->
    <div class="features-section">
      <div class="section-header">
        <h2 class="section-title">功能模块</h2>
        <p class="section-desc">选择您需要的功能</p>
      </div>
      
      <el-row :gutter="40" class="feature-row">
        <el-col :span="12" v-for="(feature, index) in features" :key="feature.title">
          <div 
            class="feature-card" 
            :class="{ 'feature-card-disabled': !feature.visible, 'feature-card-top-margin': index >= 2 }"
            @click="feature.visible && $router.push(feature.path)"
          >
            <div class="feature-icon" :style="{ background: feature.gradient }">
              <span class="icon-text">{{ feature.iconText }}</span>
            </div>
            <div class="feature-body">
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.description }}</p>
              <el-button 
                v-if="feature.visible" 
                type="primary" 
                size="small" 
                class="feature-btn"
              >
                进入
              </el-button>
              <span v-else class="feature-disabled-text">暂无权限</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const features = computed(() => {
  const userRole = userStore.user?.userRole || ''
  const isTeacherOrAdmin = userRole === 'teacher' || userRole === 'admin'
  
  console.log('用户角色:', userRole)
  console.log('是否老师或管理员:', isTeacherOrAdmin)
  console.log('用户信息:', userStore.user)
  
  return [
    {
      title: '个人中心',
      description: '管理个人信息、查看考试记录',
      iconText: '👤',
      path: '/user/profile',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      visible: userStore.isLoggedIn
    },
    {
      title: '题库管理',
      description: '浏览、创建、编辑编程题和选择题',
      iconText: '📚',
      path: '/question/list',
      gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      visible: isTeacherOrAdmin
    },
    {
      title: '考试管理',
      description: '创建考试、发布考试、查看成绩',
      iconText: '✍️',
      path: '/exam/list',
      gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
      visible: true
    },
    {
      title: '班级管理',
      description: '创建班级、管理成员、加入班级',
      iconText: '🏫',
      path: '/class/list',
      gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      visible: userStore.isLoggedIn
    },
    {
      title: '成绩查询',
      description: '查看学生考试成绩、班级成绩统计',
      iconText: '📊',
      path: '/grade/list',
      gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
      visible: isTeacherOrAdmin
    },
    {
      title: '成员管理',
      description: '管理所有用户信息、添加删除用户',
      iconText: '👥',
      path: '/admin/user',
      gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      visible: isTeacherOrAdmin
    }
  ]
})

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  userStore.initUser()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 0;
}

/* 顶部横幅 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.hero-content {
  color: white;
}

.hero-title {
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 10px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.user-name:hover {
  background: rgba(255, 255, 255, 0.3);
}

.login-btn {
  background: white;
  color: #667eea;
  border: none;
  padding: 8px 24px;
  font-weight: 500;
  border-radius: 20px;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

.register-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  padding: 8px 24px;
  border-radius: 20px;
}

.register-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 功能区域 */
.features-section {
  padding: 60px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 10px 0;
}

.section-desc {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.feature-row {
  margin-bottom: 30px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  min-height: 220px;
  display: flex;
  flex-direction: column;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.feature-card-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.feature-card-disabled:hover {
  transform: none;
}

.feature-card-top-margin {
  margin-top: 20px;
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.icon-text {
  font-size: 28px;
}

.feature-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 8px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.feature-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 16px 0;
  line-height: 1.5;
  flex: 1;
}

.feature-btn {
  border-radius: 20px;
  padding: 6px 20px;
  align-self: flex-start;
}

.feature-disabled-text {
  font-size: 12px;
  color: #c0c4cc;
}

/* 统计区域 */
.stats-section {
  padding: 0 60px 60px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-text {
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    padding: 20px;
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .hero-title {
    font-size: 24px;
  }
  
  .features-section,
  .stats-section {
    padding: 30px 20px;
  }
  
  .feature-card {
    padding: 20px;
  }
}
</style>