<template>
  <div class="wealth-page">
    <div class="header">
      <span class="header-title">理财产品</span>
    </div>
    <div class="content">
      <div class="category-tabs">
        <div
          v-for="tab in categories"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeCategory === tab.value }"
          :data-track-id="'wealth_tab_' + tab.trackId"
          @click="switchCategory(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>
      <div class="product-list">
        <div
          v-for="item in products"
          :key="item.id"
          class="product-card"
          :data-track-id="'wealth_product_click_' + item.id"
          @click="handleProductClick(item)"
        >
          <div class="product-header">
            <div class="product-name">{{ item.name }}</div>
            <span class="risk-badge">{{ item.riskLevel }}</span>
          </div>
          <div class="product-body">
            <div class="product-rate-col">
              <div class="rate-value">{{ item.annualRate }}</div>
              <div class="rate-label">{{ item.category === '保险' ? '' : '年化收益率(%)' }}</div>
            </div>
            <div class="product-detail-col">
              <div class="detail-row">
                <span class="detail-label">期限</span>
                <span class="detail-value">{{ item.term }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">起投</span>
                <span class="detail-value">{{ item.minAmount }}元</span>
              </div>
            </div>
          </div>
          <div class="product-desc">{{ item.description }}</div>
        </div>
        <div v-if="products.length === 0" class="empty">暂无产品</div>
      </div>
    </div>
    <TabBar active-tab="wealth" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TabBar from '../components/TabBar.vue'
import { getProductList } from '../api/product'

const categories = [
  { label: '全部', value: '', trackId: 'all' },
  { label: '稳健型', value: '稳健型', trackId: 'steady' },
  { label: '进取型', value: '进取型', trackId: 'aggressive' },
  { label: '基金', value: '基金', trackId: 'fund' },
  { label: '保险', value: '保险', trackId: 'insurance' }
]

const activeCategory = ref('')
const products = ref([])

const loadProducts = async () => {
  try {
    const params = { pageNum: 1, pageSize: 20 }
    if (activeCategory.value) params.category = activeCategory.value
    const res = await getProductList(params)
    if (res.code === 200) {
      products.value = res.data.content || []
    }
  } catch (e) {
    console.error('加载产品失败:', e)
  }
}

onMounted(loadProducts)

const switchCategory = (value) => {
  activeCategory.value = value
  loadProducts()
}

const handleProductClick = (item) => {
  alert('产品详情\n\n' + item.name + '\n年化：' + item.annualRate + '%\n期限：' + item.term + '\n起投：' + item.minAmount + '元\n风险：' + item.riskLevel)
}
</script>

<style scoped>
.wealth-page {
  min-height: 100vh;
  background: var(--cmb-bg);
  padding-bottom: 50px;
}
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  max-width: 375px;
  margin: 0 auto;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}
.content {
  padding: 56px 16px 16px;
  max-width: 375px;
  margin: 0 auto;
}
.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.category-tabs::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}
.tab-item.active {
  background: var(--cmb-red);
  color: #fff;
}
.product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.product-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
}
.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.risk-badge {
  font-size: 11px;
  color: #ff9800;
  background: #fff3e0;
  padding: 2px 8px;
  border-radius: 4px;
}
.product-body {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}
.product-rate-col { flex: 1; }
.rate-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--cmb-red);
}
.rate-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
.product-detail-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}
.detail-row {
  display: flex;
  gap: 6px;
  font-size: 13px;
}
.detail-label { color: #999; }
.detail-value { color: #333; }
.product-desc {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}
</style>
