import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo, getMenus } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const token = ref(localStorage.getItem('token') || '')
  const permissions = ref([])
  const role = ref('')
  const menus = ref([])

  const handleLogin = async (loginForm) => {
    const res = await login(loginForm)
    if (res.code === 200) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      userInfo.value = res.data.user
      return true
    }
    return false
  }

  const handleGetUserInfo = async () => {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      permissions.value = res.data.permissions || []
      role.value = res.data.role || ''
    }
  }

  const handleGetMenus = async () => {
    const res = await getMenus()
    if (res.code === 200) {
      menus.value = res.data || []
    }
  }

  const hasPermission = (code) => {
    if (role.value === 'ADMIN') return true
    return permissions.value.includes(code)
  }

  const handleLogout = () => {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    role.value = ''
    menus.value = []
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    token,
    permissions,
    role,
    menus,
    handleLogin,
    handleGetUserInfo,
    handleGetMenus,
    hasPermission,
    handleLogout
  }
})
