<template>
  <div class="cart">
    <div class="header">
      <h1>购物车</h1>
    </div>

    <div class="cart-list" v-if="cartList.length > 0">
      <div 
        class="cart-item" 
        v-for="item in cartList" 
        :key="item.id"
      >
        <div class="item-left">
          <el-checkbox v-model="item.checked" />
        </div>
        <img :src="item.image" :alt="item.name" />
        <div class="item-info">
          <h3 class="item-name">{{ item.name }}</h3>
          <p class="item-spec">{{ item.spec }}</p>
          <div class="item-bottom">
            <span class="price">¥{{ item.price }}</span>
            <div class="quantity">
              <button data-track-id="cart_minus" @click="changeQuantity(item, -1)">-</button>
              <span>{{ item.quantity }}</span>
              <button data-track-id="cart_plus" @click="changeQuantity(item, 1)">+</button>
            </div>
          </div>
        </div>
        <span class="delete-btn" data-track-id="cart_delete" @click="deleteItem(item)">×</span>
      </div>
    </div>

    <div class="empty-cart" v-else>
      <div class="empty-icon">🛒</div>
      <p>购物车是空的</p>
      <button data-track-id="cart_go_shopping" @click="goShopping">去购物</button>
    </div>

    <div class="cart-footer" v-if="cartList.length > 0">
      <div class="footer-left">
        <el-checkbox v-model="selectAll">全选</el-checkbox>
        <span>合计：<em>¥{{ totalPrice }}</em></span>
      </div>
      <button class="checkout-btn" data-track-id="cart_checkout" @click="checkout">
        结算({{ selectedCount }})
      </button>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElCheckbox } from 'element-plus'

const router = useRouter()
const activeTabBar = ref('cart')

const cartList = ref([
  { id: 1, name: '智能手机', spec: '128G 黑色', price: 3999, quantity: 1, checked: true, image: 'https://picsum.photos/100/100?random=20' },
  { id: 2, name: '蓝牙耳机', spec: '白色', price: 599, quantity: 2, checked: false, image: 'https://picsum.photos/100/100?random=21' }
])

const selectAll = ref(true)

const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.checked)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const selectedCount = computed(() => {
  return cartList.value.filter(item => item.checked).length
})

const changeQuantity = (item, delta) => {
  const newQuantity = item.quantity + delta
  if (newQuantity >= 1) {
    item.quantity = newQuantity
  }
}

const deleteItem = (item) => {
  const index = cartList.value.findIndex(i => i.id === item.id)
  if (index > -1) {
    cartList.value.splice(index, 1)
  }
}

const goShopping = () => {
  router.push('/product')
}

const checkout = () => {
  alert('结算功能演示')
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
.cart {
  min-height: 100vh;
  padding-bottom: 120px;
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

.cart-list {
  padding: 15px;
}

.cart-item {
  display: flex;
  align-items: center;
  background: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  position: relative;
}

.item-left {
  margin-right: 10px;
}

.cart-item img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  margin-right: 10px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.item-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 16px;
  color: #ff4757;
  font-weight: 600;
}

.quantity {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.quantity button {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  cursor: pointer;
  font-size: 16px;
}

.quantity span {
  width: 40px;
  text-align: center;
  font-size: 14px;
}

.delete-btn {
  position: absolute;
  right: 15px;
  top: 15px;
  width: 24px;
  height: 24px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 22px;
  font-size: 18px;
  cursor: pointer;
}

.empty-cart {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-cart p {
  color: #999;
  margin-bottom: 20px;
}

.empty-cart button {
  padding: 10px 30px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.cart-footer {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 15px;
  background: white;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.footer-left span {
  font-size: 14px;
  color: #666;
}

.footer-left em {
  font-style: normal;
  color: #ff4757;
  font-size: 18px;
  font-weight: 600;
}

.checkout-btn {
  padding: 10px 25px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
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
