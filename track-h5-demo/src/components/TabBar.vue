<template>
  <div class="tab-bar">
    <div
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: activeTab === tab.key }"
      :data-track-id="'tab_' + tab.key"
      @click="switchTab(tab.path)"
    >
      <div class="tab-icon" v-html="tab.icon"></div>
      <span class="tab-text">{{ tab.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  activeTab: {
    type: String,
    required: true
  }
})

const router = useRouter()

const tabs = [
  {
    key: 'home',
    label: '首页',
    path: '/',
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>'
  },
  {
    key: 'wealth',
    label: '理财',
    path: '/wealth',
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg>'
  },
  {
    key: 'life',
    label: '生活',
    path: '/life',
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>'
  },
  {
    key: 'mine',
    label: '我的',
    path: '/mine',
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>'
  }
]

const switchTab = (path) => {
  router.push(path)
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1px solid #eee;
  z-index: 100;
  max-width: 375px;
  margin: 0 auto;
}
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
}
.tab-item.active {
  color: var(--cmb-red);
}
.tab-icon {
  width: 24px;
  height: 24px;
  margin-bottom: 2px;
}
.tab-icon :deep(svg) {
  display: block;
}
.tab-text {
  font-size: 11px;
  line-height: 1;
}
</style>
