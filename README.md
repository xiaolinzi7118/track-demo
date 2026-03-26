# 埋点管理平台系统

一个完整的前端埋点解决方案，包含埋点配置管理平台、H5埋点SDK和数据回检功能。

## 项目概述

本项目是一套完整的埋点管理系统，主要包括：
- **管理后台**：用于埋点配置管理和数据查看
- **H5埋点SDK**：用于在H5页面中进行埋点数据上报
- **后端服务**：提供API接口和数据存储

## 项目结构

```
code/
├── track-backend/          # 后端服务（SpringBoot）
├── track-admin/            # 管理平台前端（Vue3）
├── track-sdk/              # H5埋点SDK
└── track-h5-demo/          # H5埋点演示页面
```

## 技术栈

### 后端
- Spring Boot 2.7.18
- Spring Data JPA
- H2 Database（内嵌）
- Lombok
- FastJSON

### 前端管理平台
- Vue 3.4
- Vue Router 4
- Pinia
- Element Plus
- Axios

### H5埋点SDK
- 原生JavaScript（无框架依赖）
- Rollup打包
- 支持UMD格式

## 快速开始

### 环境要求
- Node.js >= 16
- JDK >= 8
- Maven（可选）

---

## 后端服务启动

### 方式一：使用Maven启动

```bash
cd track-backend

# 编译并启动
mvn clean spring-boot:run
```

### 方式二：使用IDE启动
导入 `track-backend` 项目，运行 `TrackBackendApplication.java`

### 后端服务信息
- 服务端口：8080
- API文档：http://localhost:8080/
- H2数据库控制台：http://localhost:8080/h2-console
  - JDBC URL：jdbc:h2:file:./data/trackdb
  - 用户名：sa
  - 密码：（空）

### 默认账号
- 用户名：admin
- 密码：123456

---

## 管理平台前端启动

```bash
cd track-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

访问地址：http://localhost:3000

---

## H5埋点SDK使用

### SDK构建

```bash
cd track-sdk

# 安装依赖
npm install

# 构建生产版本（输出到 dist/ 目录）
npm run build

# 开发模式（监听文件变化）
npm run dev
```

### SDK集成方式

在H5页面中引入SDK：

```html
<!-- 方式一：引入本地构建的SDK -->
<script src="/path/to/track-sdk.min.js"></script>

<!-- 方式二：通过后端服务提供的SDK地址 -->
<script src="http://localhost:8080/track-sdk/track-sdk.min.js"></script>
```

初始化SDK：

```javascript
// 确保SDK加载完成后初始化
if (window.TrackSDK) {
  TrackSDK.init({
    serverUrl: 'http://localhost:8080',  // 后端服务地址
    appId: 'your-app-id',                // 应用标识
    debug: true,                         // 是否开启调试模式
    autoTrack: {
      pageView: true,                    // 自动上报页面曝光
      click: true                        // 自动追踪点击事件
    }
  })
}
```

### 手动埋点API

```javascript
// 上报自定义事件
TrackSDK.track('event_code', 'click', {
  productId: '12345',
  productName: '测试商品'
})

// 快捷方法：页面曝光
TrackSDK.trackPageView('home_page_view', {
  pageTitle: '首页'
})

// 快捷方法：点击事件
TrackSDK.trackClick('button_click', {
  buttonName: '立即购买'
})

// 设置用户信息
TrackSDK.setUserId('user_123')
TrackSDK.setUserInfo({
  nickname: '张三',
  level: 'VIP'
})
```

### 声明式埋点

在HTML元素上添加 `data-track-id` 属性，点击时会自动上报：

```html
<button data-track-id="submit_order_btn">提交订单</button>

<div data-track-id="product_item">
  <img src="product.jpg" alt="商品图片">
</div>
```

---

## H5演示页面启动

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

演示页面包含：
- 首页（商品展示、功能按钮）
- 商品列表页（搜索、筛选、商品网格）
- 购物车页（商品管理、结算）
- 用户中心页（订单、设置）

---

## 埋点配置说明

### 登录管理平台

1. 访问 http://localhost:3000
2. 使用默认账号登录（admin / 123456）

### 埋点配置步骤

1. 进入「埋点配置」页面
2. 点击「新增配置」
3. 填写配置信息：
   - **事件名称**：埋点的中文名称
   - **事件编码**：埋点的唯一标识（SDK中使用）
   - **事件类型**：页面曝光 / 点击交互
   - **生效页面URL正则**：配置该埋点在哪些页面生效（如：^https://example.com/product/.*）
   - **参数配置**：定义该埋点需要上报的参数
4. 保存配置后，SDK会自动拉取配置并生效

### 参数配置说明

- **参数名称**：参数的键名
- **参数类型**：string / number / boolean / object
- **是否必填**：是否必须上报该参数
- **描述**：参数说明

---

## 数据回检

1. 进入「数据回检」页面
2. 可通过以下条件筛选数据：
   - 事件编码
   - 事件类型
   - 用户ID
3. 点击参数列可查看详细的参数内容
4. 页面底部显示数据统计概览

---

## API接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/logout | 登出 |
| GET | /api/auth/userinfo | 获取用户信息 |
| GET | /api/auth/menus | 获取菜单 |

### 埋点配置接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/track-config/list | 获取配置列表 |
| GET | /api/track-config/all | 获取所有配置（SDK使用） |
| GET | /api/track-config/detail | 获取配置详情 |
| POST | /api/track-config/add | 新增配置 |
| POST | /api/track-config/update | 更新配置 |
| POST | /api/track-config/delete | 删除配置 |
| GET | /api/track-config/statistics | 获取配置统计 |

### 埋点数据接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/track-data/report | 单条数据上报 |
| POST | /api/track-data/batch-report | 批量数据上报 |
| GET | /api/track-data/list | 获取数据列表 |
| GET | /api/track-data/statistics | 获取数据统计 |
| GET | /api/track-data/trend | 获取趋势数据 |

---

## 数据库设计

### 系统用户表（sys_user）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| username | VARCHAR | 用户名 |
| password | VARCHAR | 密码 |
| nickname | VARCHAR | 昵称 |
| avatar | VARCHAR | 头像 |
| status | INT | 状态（0禁用/1启用） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 埋点配置表（track_config）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| event_name | VARCHAR | 事件名称 |
| event_code | VARCHAR | 事件编码 |
| event_type | VARCHAR | 事件类型 |
| description | TEXT | 描述 |
| params | TEXT | 参数配置（JSON） |
| url_pattern | VARCHAR | URL正则 |
| status | INT | 状态（0禁用/1启用） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 埋点数据表（track_data）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| event_code | VARCHAR | 事件编码 |
| event_type | VARCHAR | 事件类型 |
| url | VARCHAR | 页面URL |
| params | TEXT | 上报参数（JSON） |
| user_id | VARCHAR | 用户ID |
| session_id | VARCHAR | 会话ID |
| user_agent | VARCHAR | UA信息 |
| ip | VARCHAR | IP地址 |
| duration | BIGINT | 停留时长（ms） |
| event_time | DATETIME | 事件时间 |
| create_time | DATETIME | 创建时间 |

---

## 部署说明

### 后端部署

```bash
cd track-backend

# 打包
mvn clean package -DskipTests

# 运行jar包
java -jar target/track-backend-1.0-SNAPSHOT.jar
```

### 前端部署

```bash
# 管理平台
cd track-admin
npm run build
# 将 dist/ 目录部署到Web服务器

# H5演示
cd track-h5-demo
npm run build
# 将 dist/ 目录部署到Web服务器
```

### SDK部署

将 `track-sdk/dist/` 目录下的文件部署到CDN或静态文件服务器。

---

## 常见问题

### Q: SDK无法上报数据？
A: 请检查：
1. 后端服务是否正常运行
2. 初始化时 `serverUrl` 是否正确
3. 浏览器控制台是否有跨域错误

### Q: 配置修改后不生效？
A: SDK会在初始化时拉取配置，页面刷新后会生效。

### Q: H2数据库数据丢失？
A: H2数据库默认使用文件存储，数据保存在 `track-backend/data/` 目录，删除目录会丢失数据。

---

## 版本历史

- v1.0.0：初始版本，包含基础埋点功能

---

## 许可证

MIT
