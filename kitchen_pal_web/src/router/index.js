import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'recipes',
        name: 'Recipes',
        component: () => import('@/views/Recipes.vue'),
        meta: { title: '食谱管理' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '通知管理' }
      },
      {
        path: 'cooking-records',
        name: 'CookingRecords',
        component: () => import('@/views/CookingRecords.vue'),
        meta: { title: '烹饪记录' }
      },
      {
        path: 'ai-logs',
        name: 'AiLogs',
        component: () => import('@/views/AiLogs.vue'),
        meta: { title: 'AI识别日志' }
      },
      {
        path: 'assistant',
        name: 'Assistant',
        component: () => import('@/views/Assistant.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: 'chefs',
        name: 'Chefs',
        component: () => import('@/views/Chefs.vue'),
        meta: { title: '大厨管理' }
      },
      {
        path: 'contents',
        name: 'Contents',
        component: () => import('@/views/Contents.vue'),
        meta: { title: '内容与活动' }
      },
      {
        path: 'videos',
        name: 'Videos',
        component: () => import('@/views/Videos.vue'),
        meta: { title: '视频教学' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || 'AI厨房料理助手'} - 管理端平台`
  const isLoggedIn = localStorage.getItem('admin_logged_in')
  if (to.path !== '/login' && !isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
