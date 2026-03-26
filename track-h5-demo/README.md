# H5埋点演示页面

基于Vue3的H5埋点演示项目，展示埋点SDK的集成和使用方式。

## 项目简介

本项目是一个模拟电商H5页面，包含首页、商品列表、购物车和用户中心四个页面，用于演示埋点SDK的集成和各种埋点场景。

## 技术栈

- Vue 3.4
- Vue Router 4
- Vite

## 快速开始

```bash
cd track-h5-demo

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

访问地址：http://localhost:3001

## 项目结构

```
track-h5-demo/
├── src/
│   ├── views/
│   │   ├── Home.vue       # 首页
│   │   ├── Product.vue    # 商品列表页
│   │   ├── Cart.vue       # 购物车页
│   │   └── User.vue       # 用户中心页
│   ├── router/
│   │   └── index.js       # 路由配置
│   ├── App.vue
│   └── main.js            # 入口文件（SDK初始化）
├── index.html
├── vite.config.js
├── package.json
└── README.md
```

## SDK集成示例

### 1. 引入SDK

在 `index.html` 中引入SDK：

```html
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>H5埋点演示</title>
  <script src="http://localhost:3000/track-sdk/track-sdk.min.js"></script>
</head>
```

### 2. 初始化SDK

在 `main.js` 中初始化SDK：

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')

// SDK初始化
if (window.TrackSDK) {
  window.TrackSDK.init({
    serverUrl: 'http://localhost:3000',
    appId: 'demo-app',
    debug: true,  // 开启调试模式，控制台会输出日志
    autoTrack: {
      pageView: true,
      click: true
    }
  })
}
```

## 埋点场景示例

### 场景一：声明式点击埋点

在HTML元素上添加 `data-track-id` 属性，点击时自动上报：

```html
<!-- 首页-商品卡片点击 -->
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

<!-- 首页-功能按钮 -->
<button 
  class="btn btn-primary" 
  data-track-id="home_btn_share" 
  @click="handleShare"
>
  分享
</button>

<!-- 底部导航 -->
<div 
  class="tab-item" 
  data-track-id="tab_home"
  @click="switchTab('home')"
>
  <span class="icon">🏠</span>
  <span>首页</span>
</div>
```

### 场景二：手动调用API埋点

在需要更复杂逻辑的场景，可手动调用API上报：

```html
<template>
  <button @click="handleBuy">立即购买</button>
</template>

<script setup>
const handleBuy = (product) => {
  // 业务逻辑...
  
  // 手动上报埋点
  if (window.TrackSDK) {
    window.TrackSDK.trackClick('buy_now_click', {
      productId: product.id,
      productName: product.name,
      price: product.price
    })
  }
}
</script>
```

### 场景三：页面曝光自动埋点

当配置了 `autoTrack.pageView = true` 时，SDK会自动追踪：

1. 页面首次加载
2. 单页应用路由变化（如从首页跳转到商品页）
3. 页面可见性变化（如从后台切回）

配合管理平台的URL正则配置，可实现精细化的页面曝光统计。

### 场景四：参数上报示例

上报包含自定义参数的事件：

```javascript
// 搜索埋点
const handleSearch = (keyword) => {
  if (window.TrackSDK) {
    window.TrackSDK.trackClick('product_search', {
      keyword: keyword,
      searchTime: new Date().toISOString(),
      userId: 'user_10086'  // 可选：用户标识
    })
  }
}

// 加购埋点
const handleAddToCart = (item) => {
  if (window.TrackSDK) {
    window.TrackSDK.trackClick('cart_add', {
      productId: item.id,
      productName: item.name,
      quantity: 1,
      unitPrice: item.price,
      totalPrice: item.price
    })
  }
}
```

### 场景五：用户标识设置

```javascript
// 用户登录后设置用户ID
const onLoginSuccess = (userInfo) => {
  if (window.TrackSDK) {
    window.TrackSDK.setUserId(userInfo.userId)
    window.TrackSDK.setUserInfo({
      nickname: userInfo.nickname,
      level: userInfo.level,
      isVip: userInfo.isVip
    })
  }
}
```

## 页面埋点说明

### 首页（Home.vue）

| 埋点ID | 触发条件 | 上报参数 |
|--------|----------|----------|
| home_product_click | 点击商品卡片 | 无（可通过手动上报添加） |
| home_btn_share | 点击分享按钮 | 无 |
| home_btn_collect | 点击收藏按钮 | 无 |
| home_btn_like | 点击点赞按钮 | 无 |
| tab_home | 点击首页tab | 无 |
| tab_product | 点击商品tab | 无 |
| tab_cart | 点击购物车tab | 无 |
| tab_user | 点击我的tab | 无 |

### 商品列表页（Product.vue）

| 埋点ID | 触发条件 |
|--------|----------|
| product_search | 搜索框输入（暂未实现主动上报，需添加@input或@submit） |
| product_search_btn | 点击搜索按钮 |
| product_tab_all | 点击全部标签 |
| product_tab_new | 点击新品标签 |
| product_item_click | 点击商品卡片 |

### 购物车页（Cart.vue）

| 埋点ID | 触发条件 |
|--------|----------|
| cart_minus | 点击减号按钮 |
| cart_plus | 点击加号按钮 |
| cart_delete | 点击删除按钮 |
| cart_go_shopping | 空购物车去购物 |
| cart_checkout | 点击结算按钮 |

### 用户中心页（User.vue）

| 埋点ID | 触发条件 |
|--------|----------|
| user_order_all | 查看全部订单 |
| user_order_pay | 待付款 |
| user_order_ship | 待发货 |
| user_order_receive | 待收货 |
| user_order_review | 待评价 |
| user_order_service | 售后 |
| user_menu_address | 地址管理 |
| user_menu_coupon | 优惠券 |
| user_menu_collect | 我的收藏 |
| user_menu_history | 浏览记录 |
| user_menu_service | 客服中心 |
| user_menu_setting | 设置 |

## 查看埋点数据

1. 确保后端服务和管理平台已启动
2. 打开管理平台：http://localhost:3000
3. 登录后进入「埋点配置」页面，配置对应的埋点事件
4. 在H5页面进行操作触发埋点
5. 进入「数据回检」页面查看上报的数据

## 常见问题

### Q: 为什么点击元素没有上报？
A: 请检查：
1. SDK是否正确加载并初始化
2. 元素上的 `data-track-id` 属性是否正确
3. 控制台是否有报错信息
4. 后端服务是否正常运行

### Q: 页面曝光事件的停留时长如何统计？
A: SDK通过监听页面可见性变化和路由切换来计算停留时长，数据包含在 `page_view_leave` 事件的 `duration` 字段中。

### Q: 如何关联用户信息？
A: 用户登录后调用 `TrackSDK.setUserId('user_id')`，后续上报的所有事件都会关联该用户ID。

### Q: 单页应用路由切换会自动上报吗？
A: 是的，SDK会监听 `popstate` 事件自动检测路由变化。

## 最佳实践建议

1. **事件命名规范**：使用统一前缀，如 `page_` 页面事件、`btn_` 按钮点击、`item_` 项点击
2. **参数设计**：根据业务需要合理设计参数结构，便于后续分析
3. **关键页面优先**：从核心转化路径（如商品详情、结算）开始埋点
4. **数据验证**：测试环境验证后再发布，避免脏数据
5. **文档维护**：维护埋点字典文档，记录每个埋点的含义和用途
