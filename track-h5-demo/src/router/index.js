import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true, tabKey: 'home' }
  },
  {
    path: '/wealth',
    name: 'Wealth',
    component: () => import('../views/Wealth.vue'),
    meta: { requiresAuth: true, tabKey: 'wealth' }
  },
  {
    path: '/life',
    name: 'Life',
    component: () => import('../views/Life.vue'),
    meta: { requiresAuth: true, tabKey: 'life' }
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('../views/Mine.vue'),
    meta: { requiresAuth: true, tabKey: 'mine' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userId = localStorage.getItem('userId')
  if (to.meta.requiresAuth && !userId) {
    next('/login')
  } else if (to.path === '/login' && userId) {
    next('/')
  } else {
    next()
  }
})

export default router
