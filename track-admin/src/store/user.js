import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const token = ref(localStorage.getItem('token') || '')

  const handleLogin = async (loginForm) => {
    const res = await login(loginForm)
    if (res.code === 200) {
      token.value = 'mock-token'
      localStorage.setItem('token', 'mock-token')
      userInfo.value = res.data
      return true
    }
    return false
  }

  const handleGetUserInfo = async () => {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  }

  const handleLogout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    token,
    handleLogin,
    handleGetUserInfo,
    handleLogout
  }
})
