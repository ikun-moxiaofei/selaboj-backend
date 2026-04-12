import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/user/Register.vue')
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('../views/user/Profile.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/admin/user',
    name: 'AdminUser',
    component: () => import('../views/admin/UserManage.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/question/list',
    name: 'QuestionList',
    component: () => import('../views/question/QuestionList.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/question/add',
    name: 'QuestionAdd',
    component: () => import('../views/question/QuestionAdd.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/question/edit/:id',
    name: 'QuestionEdit',
    component: () => import('../views/question/QuestionEdit.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/question/detail/:id',
    name: 'QuestionDetail',
    component: () => import('../views/question/QuestionDetail.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/class/list',
    name: 'ClassList',
    component: () => import('../views/class/ClassList.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/class/add',
    name: 'ClassAdd',
    component: () => import('../views/class/ClassAdd.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/class/edit/:id',
    name: 'ClassEdit',
    component: () => import('../views/class/ClassEdit.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/class/detail/:id',
    name: 'ClassDetail',
    component: () => import('../views/class/ClassDetail.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exam/list',
    name: 'ExamList',
    component: () => import('../views/exam/ExamList.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exam/add',
    name: 'ExamAdd',
    component: () => import('../views/exam/ExamAdd.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exam/edit/:id',
    name: 'ExamEdit',
    component: () => import('../views/exam/ExamEdit.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exam/detail/:id',
    name: 'ExamDetail',
    component: () => import('../views/exam/ExamDetail.vue'),
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  
  const user = localStorage.getItem('user')
  const userInfo = user ? JSON.parse(user) : null
  
  if (requiresAuth && !userInfo) {
    next('/login')
  } else if (requiresAdmin && (!userInfo || userInfo.userRole !== 'admin')) {
    next('/')
  } else {
    next()
  }
})

export default router
