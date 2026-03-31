# 埋点管理平台 - 前端

基于Vue3 + Element Plus的埋点配置管理平台前端应用。

## 技术栈

- Vue 3.4
- Vue Router 4
- Pinia（状态管理）
- Element Plus
- Axios
- Vite
- Sass

## 快速开始

### 环境要求

- Node.js >= 16
- npm >= 8

### 安装依赖

```bash
cd track-admin

npm install
```

### 本地开发

```bash
npm run dev
```

访问地址：http://localhost:3000

### 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录下。

### 预览生产版本

```bash
npm run preview
```

## 项目结构

```
track-admin/
├── src/
│   ├── api/
│   │   ├── user.js              # 用户认证接口
│   │   └── track.js             # 埋点相关接口
│   ├── components/              # 公共组件
│   ├── layout/
│   │   └── index.vue            # 布局组件（侧边栏+头部）
│   ├── router/
│   │   └── index.js             # 路由配置
│   ├── store/
│   │   └── user.js              # 用户状态管理
│   ├── utils/
│   │   └── request.js           # Axios请求封装
│   ├── views/
│   │   ├── login/
│   │   │   └── index.vue        # 登录页
│   │   ├── dashboard/
│   │   │   └── index.vue        # 仪表盘
│   │   ├── track-config/
│   │   │   └── index.vue        # 埋点配置管理
│   │   ├── track-data/
│   │   │   └── index.vue        # 数据回检
│   │   └── system/
│       ├── user.vue         # 用户管理
│       └── reset-data.vue   # 重置数据
│   ├── App.vue
│   └── main.js                  # 入口文件
├── index.html
├── vite.config.js
├── package.json
└── README.md
```

## 功能模块

### 1. 用户认证

- 登录页面（默认账号：admin / 123456）
- 路由权限拦截
- Token管理

### 2. 仪表盘

- 埋点配置统计
- 上报数据统计
- 快捷入口

### 3. 埋点配置管理

- 配置列表（支持按事件类型、关键词搜索）
- 新增/编辑/删除配置
- 参数配置（参数名、类型、是否必填、描述）
- URL正则匹配规则

### 4. 数据回检

- 埋点数据列表
- 多条件筛选（事件编码、类型、用户ID）
- 参数详情展示
- 数据统计概览

### 5. 系统管理

- 用户管理
- 重置数据：提供清空埋点配置表和数据回检表的功能

## 主要页面说明

### 埋点配置表单

**事件类型说明：**
- `page_view`：页面曝光
- `click`：点击交互

**参数配置示例：**
```json
[
  {
    "name": "productId",
    "type": "string",
    "required": true,
    "description": "商品ID"
  },
  {
    "name": "price",
    "type": "number",
    "required": false,
    "description": "商品价格"
  }
]
```

**URL正则示例：**
- 匹配所有页面：（空或 `.*`）
- 匹配商品详情页：`^.*/product/\d+.*`
- 匹配特定域名：`^https://example.com/.*`

## 代理配置

`vite.config.js` 中配置了API代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true
    }
  }
}
```

如需修改后端地址，请更新此处配置。

## 环境变量

可通过 `.env` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=埋点管理平台
```

## 部署说明

### Nginx部署示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/track-admin/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 开发说明

### 添加新页面

1. 在 `src/views/` 下创建页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/layout/index.vue` 中添加菜单（如需要）

### 添加新API

在 `src/api/` 目录下的对应文件中添加接口方法：

```javascript
export function newApi(data) {
  return request({
    url: '/api/your/path',
    method: 'post',
    data
  })
}
```

## 自定义主题

可通过Element Plus主题定制修改样式，参考：
https://element-plus.org/zh-CN/guide/theming.html
