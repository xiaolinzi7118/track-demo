<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h2>埋点管理平台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        @select="handleMenuSelect"
      >
        <template v-for="menu in userStore.menus" :key="menu.id">
          <el-sub-menu v-if="menu.menuType === 1 && menu.children && menu.children.length" :index="menu.path || menu.menuCode">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="child.path"
            >
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else-if="menu.menuType === 2" :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span>欢迎使用埋点管理平台</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo.nickname || '管理员' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.handleLogout()
    router.push('/login')
  }
}

const handleMenuSelect = (index) => {
  router.push(index)
}

onMounted(async () => {
  await userStore.handleGetUserInfo()
  await userStore.fetchMenus()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}

.logo h2 {
  color: #fff;
  font-size: 16px;
  margin: 0;
}

.menu {
  border-right: none;
}

.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left span {
  font-size: 14px;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
