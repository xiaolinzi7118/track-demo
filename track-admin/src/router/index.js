import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'track-config',
        name: 'TrackConfig',
        component: () => import('../views/track-config/index.vue'),
        meta: { title: '埋点配置' }
      },
      {
        path: 'api-interface',
        name: 'ApiInterface',
        component: () => import('../views/api-interface/index.vue'),
        meta: { title: '接口来源管理' }
      },
      {
        path: 'track-data',
        name: 'TrackData',
        component: () => import('../views/track-data/index.vue'),
        meta: { title: '数据回检' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('../views/system/user.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('../views/system/role.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/reset-data',
        name: 'SystemResetData',
        component: () => import('../views/system/reset-data.vue'),
        meta: { title: '重置数据' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
