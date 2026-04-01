# 招商银行H5埋点演示 (track-h5-demo)

基于 Vue 3 + Vue Router + Vite 构建的招商银行风格移动端H5应用，用于演示埋点SDK（track-sdk）的页面浏览和点击事件追踪功能。

## 项目结构

```
track-h5-demo/
├── index.html                     # 入口HTML，加载 track-sdk
├── vite.config.js                 # Vite配置（端口3001 + API代理到8081）
├── package.json
├── track-sdk.min.js               # 埋点SDK（静态加载）
└── src/
    ├── main.js                    # 应用入口 + SDK初始化
    ├── App.vue                    # 根组件（全局样式 + CMB色彩变量）
    ├── router/index.js            # 路由配置 + 登录守卫
    ├── utils/
    │   ├── request.js             # Axios封装
    │   └── auth.js                # 登录状态管理（localStorage）
    ├── api/
    │   ├── user.js                # 用户接口（登录/登出/用户信息）
    │   ├── product.js             # 理财产品接口
    │   └── account.js             # 账户信息接口
    ├── components/
    │   └── TabBar.vue             # 共享底部导航栏
    └── views/
        ├── Login.vue              # 登录页
        ├── Home.vue               # 首页（资产总览/快捷操作/理财推荐）
        ├── Wealth.vue             # 理财产品列表页
        ├── Life.vue               # 生活服务页
        └── Mine.vue               # 我的（个人信息/菜单/退出登录）
```

## 页面说明

### 登录页 (`/login`)
- 用户名密码登录，调用 bank-backend API 验证
- 登录成功后将用户信息存储到 localStorage 并跳转首页
- 默认账号：`admin` / `123456`

### 首页 (`/`)
- 顶部红色头栏 "招商银行" + 通知铃铛
- Banner推广（金葵花理财）
- 快捷操作：转账汇款、理财产品、信用卡、贷款
- 资产总览卡片（调用账户API）
- 理财产品推荐（横向滚动卡片）

### 理财页 (`/wealth`)
- 分类筛选Tab：全部/稳健型/进取型/基金/保险
- 产品列表卡片（年化利率、期限、风险等级、起投金额）

### 生活页 (`/life`)
- 4x3服务图标网格：手机充值、生活缴费、电影票等12项服务
- 推广Banner

### 我的页 (`/mine`)
- 用户头像、昵称、金葵花客户标识
- 账户总资产显示
- 菜单列表：我的账户、交易记录、我的理财、信用卡管理、安全设置、消息中心、关于我们
- 退出登录按钮

## 快速开始

### 前置条件
- Node.js >= 16
- Java 8+
- Maven 3.6+

### 1. 启动 bank-backend（业务API，端口8081）

```bash
cd bank-backend
mvn clean spring-boot:run
```

首次启动会自动：
- 创建H2数据库 `./data/bankdb`
- 初始化种子数据（admin用户、8个理财产品、1个储蓄卡账户）

### 2. 启动 track-backend（埋点接收，端口8080）

```bash
cd track-backend
mvn clean spring-boot:run
```

### 3. 启动前端（端口3001）

```bash
cd track-h5-demo
npm install
npm run dev
```

打开 http://localhost:3001 即可体验。

## API接口

所有API由 bank-backend 提供，前端通过 Vite 代理 `/api` 到 `http://localhost:8081`。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/userinfo?userId=` | 获取用户信息 |
| GET | `/api/financial-product/list?category=&pageNum=&pageSize=` | 理财产品列表 |
| GET | `/api/financial-product/detail?id=` | 理财产品详情 |
| GET | `/api/account/summary?userId=` | 账户资产总览 |

## 埋点集成

track-sdk 通过 `index.html` 中的 `<script>` 标签静态加载，在 `main.js` 中初始化：

```javascript
window.TrackSDK.init({
  serverUrl: 'http://localhost:8080',  // 埋点数据上报地址（track-backend）
  appId: 'cmb-app',
  debug: true,
  autoTrack: {
    pageView: true,   // 自动页面浏览追踪
    click: true       // 自动点击追踪（基于 data-track-id）
  }
})
```

所有可交互元素均添加了 `data-track-id` 属性，SDK 会自动捕获匹配的点击事件并批量上报到 track-backend。

## 埋点清单

### 登录页
| 埋点ID | 触发条件 |
|--------|----------|
| login_username_input | 点击用户名输入框 |
| login_password_input | 点击密码输入框 |
| login_submit_btn | 点击登录按钮 |

### 首页
| 埋点ID | 触发条件 |
|--------|----------|
| home_banner | 点击Banner |
| home_notification | 点击通知铃铛 |
| home_action_transfer | 点击转账汇款 |
| home_action_wealth | 点击理财产品 |
| home_action_creditcard | 点击信用卡 |
| home_action_loan | 点击贷款 |
| home_account_summary | 点击资产卡片 |
| home_wealth_more | 点击更多理财 |
| home_product_recommend_{id} | 点击理财推荐产品 |

### 理财页
| 埋点ID | 触发条件 |
|--------|----------|
| wealth_tab_all | 点击全部Tab |
| wealth_tab_steady | 点击稳健型Tab |
| wealth_tab_aggressive | 点击进取型Tab |
| wealth_tab_fund | 点击基金Tab |
| wealth_tab_insurance | 点击保险Tab |
| wealth_product_click_{id} | 点击产品卡片 |

### 生活页
| 埋点ID | 触发条件 |
|--------|----------|
| life_service_recharge | 点击手机充值 |
| life_service_utility | 点击生活缴费 |
| life_service_movie | 点击电影票 |
| life_service_takeout | 点击外卖 |
| life_service_bus | 点击公交地铁 |
| life_service_bike | 点击共享单车 |
| life_service_hotel | 点击酒店 |
| life_service_flight | 点击机票 |
| life_service_hospital | 点击医疗挂号 |
| life_service_car | 点击车主服务 |
| life_service_lottery | 点击彩票 |
| life_service_more | 点击更多 |
| life_promo_banner | 点击推广Banner |
| life_promo_btn | 点击立即体验 |

### 我的页
| 埋点ID | 触发条件 |
|--------|----------|
| mine_balance_card | 点击资产卡片 |
| mine_menu_account | 点击我的账户 |
| mine_menu_transactions | 点击交易记录 |
| mine_menu_wealth | 点击我的理财 |
| mine_menu_creditcard | 点击信用卡管理 |
| mine_menu_security | 点击安全设置 |
| mine_menu_messages | 点击消息中心 |
| mine_menu_about | 点击关于我们 |
| mine_logout_btn | 点击退出登录 |

### 底部导航
| 埋点ID | 触发条件 |
|--------|----------|
| tab_home | 点击首页Tab |
| tab_wealth | 点击理财Tab |
| tab_life | 点击生活Tab |
| tab_mine | 点击我的Tab |

## 登录流程

1. 用户在登录页输入账号密码 → 调用 `POST /api/auth/login`
2. 登录成功 → 将 `userId` 和 `userInfo` 存入 localStorage
3. 路由守卫检查 `userId`，未登录自动跳转 `/login`
4. 退出登录 → 清除 localStorage → 跳转 `/login`

## 查看埋点数据

1. 确保后端服务和管理平台已启动
2. 打开管理平台：http://localhost:3000
3. 登录后进入「埋点配置」页面，配置对应的埋点事件
4. 在H5页面进行操作触发埋点
5. 进入「数据回检」页面查看上报的数据
