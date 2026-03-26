<template>
  <div class="user">
    <div class="user-header">
      <div class="avatar">
        <img src="https://picsum.photos/100/100?random=30" alt="avatar" />
      </div>
      <div class="user-info">
        <h2 class="username">用户昵称</h2>
        <p class="user-desc">普通会员</p>
      </div>
    </div>

    <div class="order-section">
      <div class="section-title">
        <span>我的订单</span>
        <span class="more" data-track-id="user_order_all">查看全部 ></span>
      </div>
      <div class="order-tabs">
        <div class="order-tab" data-track-id="user_order_pay" @click="handleOrderTab('待付款')">
          <span class="icon">💳</span>
          <span>待付款</span>
        </div>
        <div class="order-tab" data-track-id="user_order_ship" @click="handleOrderTab('待发货')">
          <span class="icon">📦</span>
          <span>待发货</span>
        </div>
        <div class="order-tab" data-track-id="user_order_receive" @click="handleOrderTab('待收货')">
          <span class="icon">🚚</span>
          <span>待收货</span>
        </div>
        <div class="order-tab" data-track-id="user_order_review" @click="handleOrderTab('待评价')">
          <span class="icon">⭐</span>
          <span>待评价</span>
        </div>
        <div class="order-tab" data-track-id="user_order_service" @click="handleOrderTab('售后')">
          <span class="icon">🔧</span>
          <span>售后</span>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item" data-track-id="user_menu_address" @click="handleMenu('地址管理')">
        <span class="menu-icon">📍</span>
        <span class="menu-text">地址管理</span>
        <span class="arrow">></span>
      </div>
      <div class="menu-item" data-track-id="user_menu_coupon" @click="handleMenu('优惠券')">
        <span class="menu-icon">🎫</span>
        <span class="menu-text">优惠券</span>
        <span class="arrow">></span>
      </div>
      <div class="menu-item" data-track-id="user_menu_collect" @click="handleMenu('我的收藏')">
        <span class="menu-icon">❤️</span>
        <span class="menu-text">我的收藏</span>
        <span class="arrow">></span>
      </div>
      <div class="menu-item" data-track-id="user_menu_history" @click="handleMenu('浏览记录')">
        <span class="menu-icon">🕐</span>
        <span class="menu-text">浏览记录</span>
        <span class="arrow">></span>
      </div>
      <div class="menu-item" data-track-id="user_menu_service" @click="handleMenu('客服中心')">
        <span class="menu-icon">💬</span>
        <span class="menu-text">客服中心</span>
        <span class="arrow">></span>
      </div>
      <div class="menu-item" data-track-id="user_menu_setting" @click="handleMenu('设置')">
        <span class="menu-icon">⚙️</span>
        <span class="menu-text">设置</span>
        <span class="arrow">></span>
      </div>
    </div>

    <div class="tab-bar">
      <div 
        class="tab-item" 
        :class="{ active: activeTabBar === 'product' }"
        @click="switchTabBar('product')"
      >
        <span class="icon">📦</span>
        <span>商品</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTabBar === 'cart' }"
        @click="switchTabBar('cart')"
      >
        <span class="icon">🛒</span>
        <span>购物车</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTabBar === 'user' }"
        @click="switchTabBar('user')"
      >
        <span class="icon">👤</span>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeTabBar = ref('user')

const handleOrderTab = (tab) => {
  console.log('点击订单Tab:', tab)
}

const handleMenu = (menu) => {
  console.log('点击菜单:', menu)
}

const switchTabBar = (tab) => {
  activeTabBar.value = tab
  const routes = {
    product: '/product',
    cart: '/cart',
    user: '/user'
  }
  router.push(routes[tab])
}
</script>

<style scoped>
.user {
  min-height: 100vh;
  padding-bottom: 60px;
  background: #f5f5f5;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar img {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-info {
  color: white;
}

.username {
  font-size: 20px;
  margin-bottom: 4px;
}

.user-desc {
  font-size: 14px;
  opacity: 0.9;
}

.order-section {
  margin: -20px 15px 15px;
  background: white;
  border-radius: 8px;
  padding: 15px;
  position: relative;
  z-index: 10;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.section-title span:first-child {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.more {
  font-size: 12px;
  color: #999;
  cursor: pointer;
}

.order-tabs {
  display: flex;
  justify-content: space-between;
}

.order-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 10px;
  cursor: pointer;
}

.order-tab .icon {
  font-size: 24px;
}

.order-tab span:last-child {
  font-size: 12px;
  color: #666;
}

.menu-section {
  background: white;
  margin: 0 15px;
  border-radius: 8px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 20px;
  margin-right: 10px;
}

.menu-text {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.arrow {
  color: #ccc;
  font-size: 14px;
}

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: white;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
  color: #999;
  font-size: 12px;
}

.tab-item.active {
  color: #409eff;
}

.tab-item .icon {
  font-size: 20px;
  margin-bottom: 2px;
}
</style>
