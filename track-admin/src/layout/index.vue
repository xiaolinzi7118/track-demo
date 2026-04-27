<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h2>埋点需求管理平台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        @select="handleMenuSelect"
      >
        <template v-for="menu in userStore.menus" :key="menu.id">
          <el-sub-menu
            v-if="menu.menuType === 1 && menu.children && menu.children.length"
            :index="menu.path || menu.menuCode"
          >
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

      <div class="tag-nav">
        <el-tag
          v-for="tab in tabStore.visitedTabs"
          :key="tab.path"
          class="nav-tag"
          :class="{ active: tab.path === tabStore.activeTab }"
          :effect="'plain'"
          :closable="tab.closable"
          @click="handleTagClick(tab.path)"
          @close="handleTagClose(tab.path)"
        >
          {{ tab.title }}
        </el-tag>
      </div>

      <el-main class="main">
        <router-view :key="route.fullPath" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useTabStore } from '../store/tab'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tabStore = useTabStore()

const activeMenu = computed(() => route.meta?.activeMenu || route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.handleLogout()
    tabStore.resetTabs()
    router.push('/login')
  }
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleTagClick = (path) => {
  if (path !== route.fullPath) {
    router.push(path)
  }
}

const handleTagClose = (path) => {
  const nextPath = tabStore.removeTab(path)
  if (path === route.fullPath && nextPath) {
    router.push(nextPath)
  }
}

onMounted(() => {
  tabStore.initTabs()
  tabStore.addTab({
    path: route.fullPath,
    title: route.meta?.title || '页面',
    closable: !route.meta?.affix && route.path !== '/dashboard'
  })
  tabStore.setActiveTab(route.fullPath)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f5f6fa;
}

.aside {
  background-color: #fff;
  border-right: 1px solid #ebeef5;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 18px;
  background-color: #ea3156;
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.2px;
  margin: 0;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu) {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-sub-menu__title),
.menu :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
  color: #333;
}

.menu :deep(.el-sub-menu__title:hover),
.menu :deep(.el-menu-item:hover) {
  color: #ea3156;
  background: #fcebef;
}

.menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
}

.menu :deep(.el-menu-item.is-active) {
  color: #ea3156;
  font-weight: 600;
  background: #fcebef;
  position: relative;
}

.menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: #ea3156;
  border-radius: 0 2px 2px 0;
}

.menu :deep(.el-menu-item .el-icon),
.menu :deep(.el-sub-menu__title .el-icon) {
  color: inherit;
}

.header {
  background-color: #ea3156;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  box-shadow: none;
}

.header-left span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
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
  color: #fff;
}

.tag-nav {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  padding: 8px 14px;
  background: #fff;
  border-top: 1px solid #e54165;
  border-bottom: 1px solid #ebeef5;
  overflow-x: auto;
}

.nav-tag {
  cursor: pointer;
  user-select: none;
  --el-tag-border-color: #dcdfe6;
  --el-tag-bg-color: #fff;
  --el-tag-text-color: #606266;
}

.nav-tag.active {
  --el-tag-border-color: #f6a8b8;
  --el-tag-bg-color: #fcebef;
  --el-tag-text-color: var(--el-color-primary);
  font-weight: 500;
}

.main {
  background-color: #f5f6fa;
  padding: 16px;
}
</style>
