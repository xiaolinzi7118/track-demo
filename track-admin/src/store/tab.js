import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'track_admin_tabs'

const buildDashboardTab = () => ({
  path: '/dashboard',
  title: '仪表盘',
  closable: false
})

export const useTabStore = defineStore('tab', () => {
  const visitedTabs = ref([buildDashboardTab()])
  const activeTab = ref('/dashboard')

  const persist = () => {
    sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ visitedTabs: visitedTabs.value, activeTab: activeTab.value })
    )
  }

  const ensureDashboardTab = () => {
    const hasDashboard = visitedTabs.value.some(tab => tab.path === '/dashboard')
    if (!hasDashboard) {
      visitedTabs.value.unshift(buildDashboardTab())
    }
    const dashboardTab = visitedTabs.value.find(tab => tab.path === '/dashboard')
    if (dashboardTab) {
      dashboardTab.closable = false
      dashboardTab.title = '仪表盘'
    }
  }

  const initTabs = () => {
    const cacheText = sessionStorage.getItem(STORAGE_KEY)
    if (!cacheText) {
      persist()
      return
    }

    try {
      const cache = JSON.parse(cacheText)
      visitedTabs.value = Array.isArray(cache.visitedTabs) && cache.visitedTabs.length
        ? cache.visitedTabs
        : [buildDashboardTab()]
      activeTab.value = cache.activeTab || '/dashboard'
    } catch (error) {
      visitedTabs.value = [buildDashboardTab()]
      activeTab.value = '/dashboard'
    }

    ensureDashboardTab()
    persist()
  }

  const addTab = ({ path, title, closable = true }) => {
    if (!path || path === '/login') return

    const exists = visitedTabs.value.find(tab => tab.path === path)
    if (exists) {
      exists.title = title || exists.title
      exists.closable = path === '/dashboard' ? false : !!closable
      persist()
      return
    }

    visitedTabs.value.push({
      path,
      title: title || '页面',
      closable: path === '/dashboard' ? false : !!closable
    })
    ensureDashboardTab()
    persist()
  }

  const setActiveTab = (path) => {
    activeTab.value = path
    persist()
  }

  const updateTabTitle = (path, title) => {
    if (!path || !title) return
    const target = visitedTabs.value.find(tab => tab.path === path)
    if (!target) return
    target.title = title
    persist()
  }

  const removeTab = (path) => {
    const index = visitedTabs.value.findIndex(tab => tab.path === path)
    if (index < 0) return activeTab.value
    if (!visitedTabs.value[index].closable) return activeTab.value

    visitedTabs.value.splice(index, 1)

    if (activeTab.value === path) {
      const next = visitedTabs.value[index] || visitedTabs.value[index - 1] || visitedTabs.value[0]
      activeTab.value = next ? next.path : '/dashboard'
    }

    ensureDashboardTab()
    persist()
    return activeTab.value
  }

  const resetTabs = () => {
    visitedTabs.value = [buildDashboardTab()]
    activeTab.value = '/dashboard'
    persist()
  }

  return {
    visitedTabs,
    activeTab,
    initTabs,
    addTab,
    setActiveTab,
    updateTabTitle,
    removeTab,
    resetTabs
  }
})
