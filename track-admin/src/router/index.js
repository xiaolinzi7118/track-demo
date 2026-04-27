import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import { useTabStore } from '../store/tab'

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
        meta: { title: '仪表盘', affix: true }
      },
      {
        path: 'event-manage',
        name: 'EventManage',
        component: () => import('../views/event-manage/index.vue'),
        meta: { title: '事件管理' }
      },
      {
        path: 'attribute-manage',
        name: 'AttributeManage',
        component: () => import('../views/attribute-manage/index.vue'),
        meta: { title: '属性管理' }
      },
      {
        path: 'track-config',
        redirect: '/event-manage'
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
        path: 'requirement-manage',
        name: 'RequirementManage',
        component: () => import('../views/requirement-manage/index.vue'),
        meta: { title: '需求管理' }
      },
      {
        path: 'requirement-manage/detail',
        name: 'RequirementDetail',
        component: () => import('../views/requirement-manage/detail.vue'),
        meta: { title: '需求详情', activeMenu: '/requirement-manage' }
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
        path: 'system/dict-param',
        name: 'SystemDictParam',
        component: () => import('../views/system/dict-param/index.vue'),
        meta: { title: '参数维护' }
      },
      {
        path: 'system/dict-param/:action',
        name: 'SystemDictParamDetail',
        component: () => import('../views/system/dict-param/detail.vue'),
        meta: { title: '参数维护详情', activeMenu: '/system/dict-param' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
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
      // 页面刷新时 store 重置，需要重新加载用户信息和菜单
      if (!userStore.userInfo.username) {
        try {
          await userStore.handleGetUserInfo()
          await userStore.fetchMenus()
        } catch (e) {
          userStore.handleLogout()
          next('/login')
          return
        }
      }
      next()
    } else {
      next('/login')
    }
  }
})

router.afterEach((to) => {
  if (to.path === '/login') return

  const tabStore = useTabStore()
  const closable = !to.meta?.affix && to.path !== '/dashboard'

  tabStore.addTab({
    path: to.fullPath,
    title: to.meta?.title || '页面',
    closable
  })
  tabStore.setActiveTab(to.fullPath)
})

export default router
