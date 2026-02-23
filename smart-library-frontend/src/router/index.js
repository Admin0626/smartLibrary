import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/BooksView.vue'),
    meta: { title: '图书列表', requiresAuth: true }
  },
  {
    path: '/my-borrows',
    name: 'MyBorrows',
    component: () => import('@/views/MyBorrowsView.vue'),
    meta: { title: '我的借阅', requiresAuth: true }
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: () => import('@/views/MyReservationsView.vue'),
    meta: { title: '我的预约', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧图书馆` : '智慧图书馆'

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn()) {
      next('/login')
      return
    }

    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin()) {
      next('/home')
      return
    }
  }

  // 如果已登录，访问登录/注册页面则跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn()) {
    next('/home')
    return
  }

  next()
})

export default router
