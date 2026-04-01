<template>
  <div class="mine-page">
    <div class="mine-header">
      <div class="user-info">
        <div class="avatar">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="#fff"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
        </div>
        <div class="user-detail">
          <div class="user-name">{{ userInfo.nickname || userInfo.username || '用户' }}</div>
          <div class="user-level">
            <span class="level-badge">金葵花客户</span>
          </div>
        </div>
      </div>
    </div>

    <div class="content">
      <!-- 账户快览 -->
      <div class="balance-card" data-track-id="mine_balance_card">
        <div class="balance-label">总资产（元）</div>
        <div class="balance-value">{{ totalBalance }}</div>
      </div>

      <!-- 菜单列表 -->
      <div class="menu-card">
        <div class="menu-item" data-track-id="mine_menu_account" @click="handleMenu('我的账户')">
          <span class="menu-icon">💳</span>
          <span class="menu-text">我的账户</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_transactions" @click="handleMenu('交易记录')">
          <span class="menu-icon">📋</span>
          <span class="menu-text">交易记录</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_wealth" @click="$router.push('/wealth')">
          <span class="menu-icon">📈</span>
          <span class="menu-text">我的理财</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_creditcard" @click="handleMenu('信用卡管理')">
          <span class="menu-icon">💳</span>
          <span class="menu-text">信用卡管理</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_security" @click="handleMenu('安全设置')">
          <span class="menu-icon">🔒</span>
          <span class="menu-text">安全设置</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_messages" @click="handleMenu('消息中心')">
          <span class="menu-icon">🔔</span>
          <span class="menu-text">消息中心</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" data-track-id="mine_menu_about" @click="handleMenu('关于我们')">
          <span class="menu-icon">ℹ️</span>
          <span class="menu-text">关于我们</span>
          <span class="menu-arrow">></span>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-btn" data-track-id="mine_logout_btn" @click="handleLogout">
        退出登录
      </div>
    </div>

    <TabBar active-tab="mine" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '../components/TabBar.vue'
import { getUserInfo } from '../api/user'
import { getAccountSummary } from '../api/account'
import { getUserId, getUserInfo as getStoredUserInfo, clearAuth } from '../utils/auth'

const router = useRouter()
const userInfo = ref({})
const totalBalance = ref('***.**')

onMounted(async () => {
  const userId = getUserId()
  const stored = getStoredUserInfo()
  if (stored) {
    userInfo.value = stored
  }
  if (userId) {
    try {
      const [userRes, accountRes] = await Promise.all([
        getUserInfo(userId),
        getAccountSummary(userId)
      ])
      if (userRes.code === 200 && userRes.data) {
        userInfo.value = { ...userInfo.value, ...userRes.data }
      }
      if (accountRes.code === 200 && accountRes.data) {
        totalBalance.value = accountRes.data.totalBalance.toLocaleString('zh-CN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        })
      }
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }
})

const handleMenu = (name) => {
  alert('功能演示：' + name)
}

const handleLogout = () => {
  if (confirm('确定退出登录吗？')) {
    clearAuth()
    router.push('/login')
  }
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background: var(--cmb-bg);
  padding-bottom: 50px;
}
.mine-header {
  background: linear-gradient(135deg, #d4282d, #b91c22);
  padding: 56px 20px 30px;
  max-width: 375px;
  margin: 0 auto;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-detail {
  color: #fff;
}
.user-name {
  font-size: 18px;
  font-weight: 600;
}
.user-level {
  margin-top: 4px;
}
.level-badge {
  display: inline-block;
  font-size: 11px;
  background: linear-gradient(135deg, #c9a96e, #e0c38c);
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
}
.content {
  padding: 0 16px 16px;
  max-width: 375px;
  margin: 0 auto;
}
.balance-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: -16px;
  position: relative;
  margin-bottom: 12px;
}
.balance-label {
  font-size: 13px;
  color: #999;
}
.balance-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-top: 6px;
}
.menu-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-item:active {
  background: #f9f9f9;
}
.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}
.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}
.menu-arrow {
  color: #ccc;
  font-size: 14px;
}
.logout-btn {
  text-align: center;
  padding: 14px;
  background: #fff;
  border-radius: 8px;
  color: var(--cmb-red);
  font-size: 15px;
  cursor: pointer;
}
.logout-btn:active {
  background: #f9f9f9;
}
</style>
