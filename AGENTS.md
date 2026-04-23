本文件为 AI 编程助手提供项目上下文和操作指南。

## 项目概述

本项目是一套完整的埋点需求管理平台系统，主要包括：
- **管理后台**：用于埋点配置管理和数据查看
- **H5埋点SDK**：用于在H5页面中进行埋点数据上报
- **后端服务**：提供API接口和数据存储

## 项目结构

```
code/
├── track-backend/          # 后端服务（SpringBoot）
├── track-admin/            # 管理平台前端（Vue3）
├── track-sdk/              # H5埋点SDK
├── bank-backend/           # H5演示页面后端服务
└── track-h5-demo/          # H5埋点演示页面
```

### H5埋点SDK
- 原生JavaScript（无框架依赖）
- Rollup打包
- 支持UMD格式

## 后端服务启动

### 方式一：使用Maven启动

```bash
cd track-backend
# 使用Maven安装依赖
mvn clean install
# 编译并启动
mvn clean spring-boot:run
```

### 方式二：使用IDE启动
导入 `track-backend` 项目，运行 `TrackBackendApplication.java`

## 打包
mvn clean package -DskipTests
## 运行jar包
java -jar target/track-backend-1.0-SNAPSHOT.jar
```

### 后端服务信息
- 服务端口：8080

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

## 常见开发任务规范

- 涉及到数据库表结构或字段变更，必须更新表结构设计文档到track-backend\docs目录下，以及更新track-backend\src\main\resources\db\mysql目录下的建表SQL脚本+初始化数据脚本+权限初始化脚本，方便后面初始化部署；以及在track-backen\docs目录下输出此次相关变更的sql脚本。