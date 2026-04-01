<template>
  <div class="life-page">
    <div class="header">
      <span class="header-title">生活</span>
    </div>
    <div class="content">
      <div class="service-grid">
        <div
          v-for="item in services"
          :key="item.trackId"
          class="service-item"
          :data-track-id="'life_service_' + item.trackId"
          @click="handleService(item.name)"
        >
          <div class="service-icon" :style="{ background: item.color }">
            <span class="icon-emoji">{{ item.icon }}</span>
          </div>
          <span class="service-name">{{ item.name }}</span>
        </div>
      </div>
      <div class="promo-card" data-track-id="life_promo_banner">
        <div class="promo-inner">
          <div class="promo-title">招商银行App</div>
          <div class="promo-desc">生活缴费 简单便捷</div>
          <div class="promo-action" data-track-id="life_promo_btn" @click="handleService('立即体验')">立即体验</div>
        </div>
      </div>
    </div>
    <TabBar active-tab="life" />
  </div>
</template>

<script setup>
import TabBar from '../components/TabBar.vue'

const services = [
  { name: '手机充值', icon: '\u{1F4F1}', color: '#e3f2fd', trackId: 'recharge' },
  { name: '生活缴费', icon: '\u{1F4A1}', color: '#fff3e0', trackId: 'utility' },
  { name: '电影票', icon: '\u{1F3AC}', color: '#fce4ec', trackId: 'movie' },
  { name: '外卖', icon: '\u{1F35C}', color: '#e8f5e9', trackId: 'takeout' },
  { name: '公交地铁', icon: '\u{1F68C}', color: '#e0f7fa', trackId: 'bus' },
  { name: '共享单车', icon: '\u{1F6B2}', color: '#f1f8e9', trackId: 'bike' },
  { name: '酒店', icon: '\u{1F3E8}', color: '#ede7f6', trackId: 'hotel' },
  { name: '机票', icon: '\u2708\uFE0F', color: '#e8eaf6', trackId: 'flight' },
  { name: '医疗挂号', icon: '\u{1F3E5}', color: '#ffebee', trackId: 'hospital' },
  { name: '车主服务', icon: '\u{1F697}', color: '#e0f2f1', trackId: 'car' },
  { name: '彩票', icon: '\u{1F3AB}', color: '#fff8e1', trackId: 'lottery' },
  { name: '更多', icon: '\u2795', color: '#f5f5f5', trackId: 'more' }
]

const handleService = (name) => {
  alert('功能演示：' + name)
}
</script>

<style scoped>
.life-page {
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
.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 8px;
  background: #fff;
  border-radius: 8px;
  padding: 20px 12px;
  margin-bottom: 16px;
}
.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}
.service-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-emoji {
  font-size: 22px;
}
.service-name {
  font-size: 12px;
  color: #333;
  margin-top: 6px;
}
.promo-card {
  border-radius: 8px;
  overflow: hidden;
}
.promo-inner {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px;
  padding: 20px;
  color: #fff;
}
.promo-title {
  font-size: 18px;
  font-weight: 600;
}
.promo-desc {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}
.promo-action {
  display: inline-block;
  margin-top: 12px;
  padding: 6px 16px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}
</style>
