# H5埋点SDK

轻量级H5页面埋点上报SDK，支持页面曝光、点击交互等多种埋点类型。

## 功能特性

- ✅ 页面曝光自动追踪（含停留时长统计）
- ✅ 点击交互自动追踪（声明式，受埋点配置控制）
- ✅ 自定义事件上报
- ✅ 批量上报与失败重试
- ✅ 会话管理与用户标识
- ✅ 配置动态拉取与URL匹配
- ✅ 无框架依赖，支持原生JS和各种前端框架

## 快速集成

### 方式一：Script标签引入

```html
<script src="http://your-cdn-path/track-sdk.min.js"></script>
<script>
  // 初始化SDK
  TrackSDK.init({
    serverUrl: 'http://localhost:8080',  // 后端服务地址
    appId: 'your-app-id',                // 应用标识
    debug: true,                         // 调试模式（开启控制台日志）
    autoTrack: {
      pageView: true,                    // 自动追踪页面曝光
      click: true                        // 自动追踪点击事件
    }
  })
</script>
```

### 方式二：模块化引入（待发布）

```bash
npm install track-sdk
```

```javascript
import TrackSDK from 'track-sdk'

TrackSDK.init({
  serverUrl: 'http://localhost:8080',
  appId: 'your-app-id'
})
```

## 构建说明

```bash
cd track-sdk

# 安装依赖
npm install

# 构建生产版本
npm run build
```

构建产物位于 `dist/` 目录：
- `track-sdk.js` - 未压缩版本（用于开发调试）
- `track-sdk.min.js` - 压缩版本（用于生产）

## 项目结构

```
track-sdk/
├── src/
│   ├── index.js       # SDK入口文件
│   ├── core.js        # 核心逻辑
│   └── utils.js       # 工具函数
├── dist/              # 构建产物目录
├── rollup.config.js   # Rollup配置
├── package.json
└── README.md
```

## API文档

### 初始化配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| serverUrl | string | - | 后端服务地址 |
| appId | string | - | 应用标识 |
| debug | boolean | false | 是否开启调试模式 |
| autoTrack | object | {pageView: true, click: true} | 自动追踪配置 |

### 手动上报API

#### TrackSDK.track(eventCode, eventType, params)

自定义事件上报

```javascript
TrackSDK.track('button_click', 'click', {
  buttonName: '立即购买',
  productId: '12345'
})
```

#### TrackSDK.trackPageView(eventCode, params)

页面曝光事件快捷上报

```javascript
TrackSDK.trackPageView('home_page', {
  pageTitle: '首页',
  channel: 'wechat'
})
```

#### TrackSDK.trackClick(eventCode, params)

点击事件快捷上报

```javascript
TrackSDK.trackClick('banner_click', {
  bannerId: 'banner_001',
  bannerName: '促销活动'
})
```

### 用户标识API

#### TrackSDK.setUserId(userId)

设置用户ID

```javascript
// 用户登录后设置
TrackSDK.setUserId('user_10086')
```

#### TrackSDK.setUserInfo(userInfo)

设置用户信息

```javascript
TrackSDK.setUserInfo({
  nickname: '张三',
  level: 'VIP',
  isNew: false
})
```

#### TrackSDK.getSessionId()

获取当前会话ID

```javascript
const sessionId = TrackSDK.getSessionId()
console.log('当前会话ID:', sessionId)
```

## 声明式埋点

在需要追踪点击的HTML元素上添加 `data-track-id` 属性：

```html
<!-- 按钮点击 -->
<button data-track-id="submit_order">提交订单</button>

<!-- 商品卡片点击 -->
<div class="product-card" data-track-id="product_click">
  <img src="product.jpg" alt="商品图片">
  <p>商品名称</p>
</div>

<!-- 导航点击（支持多级元素查找） -->
<div class="nav-item" data-track-id="nav_home">
  <i class="icon-home"></i>
  <span>首页</span>
</div>
```

**重要说明**：
SDK会自动查找点击元素及其父元素上的 `data-track-id` 属性，但只有在埋点配置中存在对应的 `trackId` 且状态为启用时，才会上报点击事件。

配置要求：
1. 在埋点管理后台创建点击类型的埋点配置
2. 配置 `trackId` 字段，值与HTML元素上的 `data-track-id` 一致
3. 确保配置状态为"启用"（status = 1）

## 自动上报说明

### 页面曝光事件

当 `autoTrack.pageView = true` 时：

1. 页面加载完成后自动上报
2. 单页应用路由变化时自动上报
3. 页面可见性变化（从后台切回）时自动上报
4. 自动记录页面停留时长

**匹配规则**：
SDK在初始化时会从后端拉取所有埋点配置，当页面URL匹配配置中的 `urlPattern` 且事件类型为 `page_view` 时，才会触发对应的自动上报。

事件数据包含：
```javascript
{
  eventCode: "page_view",           // 从配置中匹配得到
  eventType: "page_view",
  url: "https://example.com/page",
  params: JSON.stringify({
    pageTitle: "页面标题"
  }),
  duration: 5000,                    // 停留时长（毫秒）
  eventTime: "2024-01-01T12:00:00.000Z"
}
```

### 点击事件

当 `autoTrack.click = true` 时：

1. 监听整个文档的点击事件
2. 查找点击元素及其父元素上的 `data-track-id` 属性
3. 从埋点配置中匹配对应 `trackId` 的配置
4. 只有找到匹配的配置且状态为启用时才上报

**匹配规则**：
- 事件类型必须为 `click`
- 配置中的 `trackId` 必须与HTML元素上的 `data-track-id` 完全匹配
- 配置状态必须为"启用"（status = 1）

### URL匹配规则

对于页面曝光事件，SDK在初始化时会从后端拉取所有埋点配置，当页面URL匹配配置中的 `urlPattern` 时，才会触发对应的自动上报。

匹配规则示例：

| URL Pattern | 匹配说明 | 匹配示例 |
|-------------|----------|----------|
| （空） | 匹配所有页面 | 任何页面 |
| `.*` | 匹配所有页面 | 任何页面 |
| `^https://example.com/product` | 匹配商品相关页面 | https://example.com/product/123 |
| `^.*/cart$` | 匹配购物车页面 | https://example.com/cart |
| `^.*/user.*` | 匹配用户中心页面 | https://example.com/user/center |

## 上报策略

### 批量上报

SDK内部维护一个事件队列，满足以下条件之一时触发上报：

1. 队列长度达到20条
2. 距离上次上报超过5秒

### 失败重试

上报失败时，SDK会将数据放回队列，等待下次上报时机重试。

### 页面卸载处理

页面卸载（刷新、关闭、跳转）时，SDK会立即上报队列中剩余数据，使用 `navigator.sendBeacon` API确保数据可靠发送。

## 数据字段说明

| 字段 | 说明 | 示例值 |
|------|------|--------|
| eventCode | 事件编码（埋点标识） | `home_page_view` |
| eventType | 事件类型 | `page_view` / `click` |
| url | 页面完整URL | `https://example.com/path` |
| params | 自定义参数（JSON字符串） | `{"productId":"123"}` |
| userId | 用户ID（调用setUserId后） | `user_10086` |
| sessionId | 会话ID（页面关闭前不变） | `a1b2c3d4-e5f6-...` |
| userAgent | 浏览器UA字符串 | `Mozilla/5.0 (iPhone; ...` |
| ip | 客户端IP（后端填充） | `192.168.1.1` |
| duration | 停留时长（毫秒） | `5000` |
| eventTime | 事件发生时间 | `2024-01-01T12:00:00.000Z` |

## 调试技巧

开启debug模式后，SDK会在控制台输出详细的调试信息：

```javascript
TrackSDK.init({
  serverUrl: 'http://localhost:8080',
  appId: 'demo',
  debug: true  // 开启调试
})
```

控制台输出示例：
```
[TrackSDK] initialized successfully {serverUrl: "...", ...}
[TrackSDK] Track config fetched: [{...}, {...}]
[TrackSDK] Track event added to queue: {...}
[TrackSDK] Track data reported successfully: [{...}]
[TrackSDK] Click event not tracked - no matching config found for trackId: button_click
```

## 注意事项

1. **跨域问题**：确保后端服务配置了正确的CORS策略，允许前端页面域名访问
2. **HTTPS要求**：生产环境建议使用HTTPS，避免混合内容警告
3. **数据合规**：请确保遵守当地数据保护法规（如GDPR、CCPA等）
4. **性能影响**：SDK对性能影响极小，但建议不要过度埋点
5. **测试环境**：建议在测试环境验证埋点配置后再发布到生产
6. **点击事件配置**：点击事件需要在埋点配置中配置对应的 `trackId`，否则不会上报

## 版本历史

- v1.1.0
  - 点击事件现在受埋点配置控制，需要配置对应的 `trackId`
  - 添加了调试日志，方便排查点击事件未上报的问题

- v1.0.0
  - 初始版本
  - 支持页面曝光和点击事件追踪
  - 支持声明式埋点和手动上报API
