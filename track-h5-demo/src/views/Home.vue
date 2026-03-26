<template>
  <div class="home">
    <div class="header">
      <h1>埋点演示首页</h1>
      <p>体验页面曝光和点击交互埋点</p>
    </div>
    
    <div class="banner">
      <img src="https://picsum.photos/400/200" alt="banner" />
    </div>

    <div class="section">
      <h2>热门商品</h2>
      <div class="product-list">
        <div 
          class="product-item" 
          v-for="item in products" 
          :key="item.id"
          data-track-id="home_product_click"
          @click="goToDetail(item)"
        >
          <img :src="item.image" :alt="item.name" />
          <p class="product-name">{{ item.name }}</p>
          <p class="product-price">¥{{ item.price }}</p>
        </div>
      </div>
    </div>

    <div class="section">
      <h2>功能按钮</h2>
      <div class="button-group">
        <button class="btn btn-primary" data-track-id="home_btn_share" @click="handleShare">
          分享
        </button>
        <button class="btn btn-success" data-track-id="home_btn_collect" @click="handleCollect">
          收藏
        </button>
        <button class="btn btn-warning" data-track-id="home_btn_like" @click="handleLike">
          点赞
        </button>
      </div>
    </div>

    <div class="tab-bar">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'home' }"
        data-track-id="tab_home"
        @click="switchTab('home')"
      >
        <span class="icon">🏠</span>
        <span>首页</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'product' }"
        data-track-id="tab_product"
        @click="switchTab('product')"
      >
        <span class="icon">📦</span>
        <span>商品</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'cart' }"
        data-track-id="tab_cart"
        @click="switchTab('cart')"
      >
        <span class="icon">🛒</span>
        <span>购物车</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'user' }"
        data-track-id="tab_user"
        @click="switchTab('user')"
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
const activeTab = ref('home')

const products = ref([
  { id: 1, name: '商品1', price: 99, image: 'https://picsum.photos/150/150?random=1' },
  { id: 2, name: '商品2', price: 199, image: 'https://picsum.photos/150/150?random=2' },
  { id: 3, name: '商品3', price: 299, image: 'https://picsum.photos/150/150?random=3' },
  { id: 4, name: '商品4', price: 399, image: 'https://picsum.photos/150/150?random=4' }
])

const goToDetail = (item) => {
  console.log('点击商品:', item)
}

const handleShare = () => {
  alert('点击了分享按钮')
}

const handleCollect = () => {
  alert('点击了收藏按钮')
}

const handleLike = () => {
  alert('点击了点赞按钮')
}

const switchTab = (tab) => {
  activeTab.value = tab
  const routes = {
    home: '/',
    product: '/product',
    cart: '/cart',
    user: '/user'
  }
  router.push(routes[tab])
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding-bottom: 60px;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px 20px;
  text-align: center;
}

.header h1 {
  font-size: 24px;
  margin-bottom: 8px;
}

.header p {
  font-size: 14px;
  opacity: 0.9;
}

.banner {
  padding: 15px;
}

.banner img {
  width: 100%;
  border-radius: 8px;
}

.section {
  padding: 15px;
}

.section h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.product-item {
  background: white;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-item img {
  width: 100%;
  border-radius: 4px;
  margin-bottom: 8px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.product-price {
  font-size: 16px;
  color: #ff4757;
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  color: white;
  cursor: pointer;
}

.btn-primary {
  background: #409eff;
}

.btn-success {
  background: #67c23a;
}

.btn-warning {
  background: #e6a23c;
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
