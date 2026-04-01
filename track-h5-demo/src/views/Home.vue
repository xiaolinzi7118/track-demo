<template>
  <div class="home-page">
    <!-- 头部 -->
    <div class="header">
      <span class="header-title">招商银行</span>
      <div class="header-right" data-track-id="home_notification" @click="handleNotification">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="#fff"><path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content">
      <!-- Banner -->
      <div class="banner" data-track-id="home_banner">
        <div class="banner-inner">
          <div class="banner-text">
            <div class="banner-title">金葵花理财</div>
            <div class="banner-desc">稳健增值 专业陪伴</div>
          </div>
          <div class="banner-rate">最高年化 <span>4.10%</span></div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="section">
        <div class="quick-actions">
          <div class="action-item" data-track-id="home_action_transfer" @click="handleAction('转账汇款')">
            <div class="action-icon transfer-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff"><path d="M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z"/></svg>
            </div>
            <span>转账汇款</span>
          </div>
          <div class="action-item" data-track-id="home_action_wealth" @click="$router.push('/wealth')">
            <div class="action-icon wealth-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg>
            </div>
            <span>理财产品</span>
          </div>
          <div class="action-item" data-track-id="home_action_creditcard" @click="handleAction('信用卡')">
            <div class="action-icon card-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff"><path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
            </div>
            <span>信用卡</span>
          </div>
          <div class="action-item" data-track-id="home_action_loan" @click="handleAction('贷款')">
            <div class="action-icon loan-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
            </div>
            <span>贷款</span>
          </div>
        </div>
      </div>

      <!-- 资产总览 -->
      <div class="section">
        <div class="account-card" data-track-id="home_account_summary">
          <div class="account-label">我的资产（元）</div>
          <div class="account-balance">{{ accountBalance }}</div>
        </div>
      </div>

      <!-- 理财推荐 -->
      <div class="section">
        <div class="section-header">
          <span class="section-title">理财推荐</span>
          <span class="section-more" data-track-id="home_wealth_more" @click="$router.push('/wealth')">更多 ></span>
        </div>
        <div class="product-scroll">
          <div
            v-for="item in products"
            :key="item.id"
            class="product-card"
            :data-track-id="'home_product_recommend_' + item.id"
            @click="handleProductClick(item)"
          >
            <div class="product-name">{{ item.name }}</div>
            <div class="product-rate">
              <span class="rate-value">{{ item.annualRate }}</span>
              <span class="rate-unit">%{{ item.category === '保险' ? '' : '年化' }}</span>
            </div>
            <div class="product-info">
              <span class="risk-badge">{{ item.riskLevel }}</span>
              <span class="product-term">{{ item.term }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <TabBar active-tab="home" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '../components/TabBar.vue'
import { getAccountSummary } from '../api/account'
import { getProductList } from '../api/product'
import { getUserId } from '../utils/auth'

const router = useRouter()
const accountBalance = ref('***.**')
const products = ref([])

onMounted(async () => {
  const userId = getUserId()
  if (userId) {
    try {
      const [accountRes, productRes] = await Promise.all([
        getAccountSummary(userId),
        getProductList({ pageNum: 1, pageSize: 4 })
      ])
      if (accountRes.code === 200 && accountRes.data) {
        accountBalance.value = accountRes.data.totalBalance.toLocaleString('zh-CN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        })
      }
      if (productRes.code === 200) {
        products.value = productRes.data.content || []
      }
    } catch (e) {
      console.error('加载数据失败:', e)
    }
  }
})

const handleNotification = () => {
  alert('暂无新消息')
}

const handleAction = (name) => {
  alert('功能演示：' + name)
}

const handleProductClick = (item) => {
  alert('查看产品：' + item.name + '\n年化利率：' + item.annualRate + '%\n期限：' + item.term + '\n风险等级：' + item.riskLevel)
}
</script>

<style scoped>
.home-page {
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
  background: var(--cmb-red);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;
  max-width: 375px;
  margin: 0 auto;
}
.header-title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}
.header-right {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.content {
  padding: 56px 16px 16px;
  max-width: 375px;
  margin: 0 auto;
}
.banner {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}
.banner-inner {
  background: linear-gradient(135deg, #d4282d, #e85d50);
  border-radius: 8px;
  padding: 20px;
  color: #fff;
}
.banner-title {
  font-size: 18px;
  font-weight: 600;
}
.banner-desc {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}
.banner-rate {
  margin-top: 12px;
  font-size: 13px;
}
.banner-rate span {
  font-size: 22px;
  font-weight: 700;
}
.section {
  margin-bottom: 16px;
}
.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}
.action-item span {
  font-size: 12px;
  color: #333;
  margin-top: 6px;
}
.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.transfer-icon { background: linear-gradient(135deg, #ff7e5f, #feb47b); }
.wealth-icon { background: linear-gradient(135deg, #d4282d, #e85d50); }
.card-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.loan-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.account-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.account-label {
  font-size: 13px;
  color: #999;
}
.account-balance {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-top: 8px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.section-more {
  font-size: 13px;
  color: #999;
  cursor: pointer;
}
.product-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
}
.product-scroll::-webkit-scrollbar {
  display: none;
}
.product-card {
  min-width: 160px;
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  flex-shrink: 0;
}
.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-rate {
  margin-top: 8px;
}
.rate-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--cmb-red);
}
.rate-unit {
  font-size: 12px;
  color: var(--cmb-red);
}
.product-info {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.risk-badge {
  font-size: 11px;
  color: #ff9800;
  background: #fff3e0;
  padding: 2px 6px;
  border-radius: 4px;
}
.product-term {
  font-size: 12px;
  color: #999;
}
</style>
