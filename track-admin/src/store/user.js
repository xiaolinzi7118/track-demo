import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '../api/user'
import { getUserMenus } from '../api/menu'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const token = ref(localStorage.getItem('token') || '')
  const menus = ref([])
  const permissions = ref([])

  const handleLogin = async (loginForm) => {
    const res = await login(loginForm)
    if (res.code === 200) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      await handleGetUserInfo()
      await fetchMenus()
      return true
    }
    return false
  }

  const handleGetUserInfo = async () => {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      permissions.value = res.data.permissions || []
    }
  }

  const fetchMenus = async () => {
    const res = await getUserMenus()
    if (res.code === 200) {
      menus.value = res.data || []
    }
  }

  const hasPermission = (perm) => {
    if (userInfo.value.isSuperAdmin === true) return true
    return permissions.value.includes(perm)
  }

  const handleLogout = () => {
    token.value = ''
    userInfo.value = {}
    menus.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  return {
    userInfo, token, menus, permissions,
    handleLogin, handleGetUserInfo, fetchMenus, hasPermission, handleLogout
  }
})
