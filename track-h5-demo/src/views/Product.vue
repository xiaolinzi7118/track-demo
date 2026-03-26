<template>
  <div class="product">
    <div class="header">
      <h1>商品列表</h1>
    </div>

    <div class="search-bar">
      <input type="text" placeholder="搜索商品..." data-track-id="product_search" />
      <button data-track-id="product_search_btn">搜索</button>
    </div>

    <div class="filter-tabs">
      <span 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="{ active: activeTab === tab.id }"
        :data-track-id="'product_tab_' + tab.id"
        @click="switchTab(tab.id)"
      >
        {{ tab.name }}
      </span>
    </div>

    <div class="product-grid">
      <div 
        class="product-card" 
        v-for="item in productList" 
        :key="item.id"
        data-track-id="product_item_click"
        @click="viewDetail(item)"
      >
        <img :src="item.image" :alt="item.name" />
        <div class="product-info">
          <h3 class="product-name">{{ item.name }}</h3>
          <p class="product-desc">{{ item.desc }}</p>
          <div class="product-bottom">
            <span class="price">¥{{ item.price }}</span>
            <span class="sales">已售{{ item.sales }}件</span>
          </div>
        </div>
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
const activeTab = ref('all')
const activeTabBar = ref('product')

const tabs = ref([
  { id: 'all', name: '全部' },
  { id: 'new', name: '新品' },
  { id: 'hot', name: '热销' },
  { id: 'discount', name: '优惠' }
])

const productList = ref([
  { id: 1, name: '智能手机', desc: '最新款旗舰手机', price: 3999, sales: 1234, image: 'https://picsum.photos/200/200?random=10' },
  { id: 2, name: '蓝牙耳机', desc: '降噪无线耳机', price: 599, sales: 5678, image: 'https://picsum.photos/200/200?random=11' },
  { id: 3, name: '智能手表', desc: '健康运动监测', price: 1299, sales: 2345, image: 'https://picsum.photos/200/200?random=12' },
  { id: 4, name: '平板电脑', desc: '轻薄便携办公', price: 2999, sales: 876, image: 'https://picsum.photos/200/200?random=13' },
  { id: 5, name: '机械键盘', desc: 'RGB背光游戏键盘', price: 399, sales: 3456, image: 'https://picsum.photos/200/200?random=14' },
  { id: 6, name: '无线鼠标', desc: '人体工学设计', price: 199, sales: 7890, image: 'https://picsum.photos/200/200?random=15' }
])

const switchTab = (tabId) => {
  activeTab.value = tabId
}

const viewDetail = (item) => {
  console.log('查看商品详情:', item)
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
.product {
  min-height: 100vh;
  padding-bottom: 60px;
  background: #f5f5f5;
}

.header {
  background: white;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.header h1 {
  font-size: 18px;
  color: #333;
  text-align: center;
}

.search-bar {
  display: flex;
  padding: 10px 15px;
  background: white;
  gap: 10px;
}

.search-bar input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.search-bar button {
  padding: 10px 20px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.filter-tabs {
  display: flex;
  padding: 10px 15px;
  background: white;
  border-bottom: 1px solid #eee;
  gap: 20px;
  overflow-x: auto;
}

.filter-tabs span {
  flex-shrink: 0;
  padding: 5px 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
}

.filter-tabs span.active {
  background: #409eff;
  color: white;
}

.product-grid {
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-card img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.product-info {
  padding: 10px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.product-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 16px;
  color: #ff4757;
  font-weight: 600;
}

.sales {
  font-size: 12px;
  color: #999;
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
