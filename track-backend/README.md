# 埋点管理平台 - 后端服务

## 项目简介

基于Spring Boot的埋点管理平台后端服务，提供用户认证、埋点配置管理、数据上报和查询功能。

## 技术栈

- Spring Boot 2.7.18
- Spring Data JPA
- H2 Database（内嵌数据库）
- Lombok
- FastJSON

## 快速开始

### 环境要求

- JDK 8 或以上
- Maven 3.6 或以上

### 本地启动

```bash
# 进入项目目录
cd track-backend
# 使用Maven安装依赖
mvn clean install
# 使用Maven启动
mvn clean spring-boot:run

# 或先打包再运行
mvn clean package -DskipTests
java -jar target/track-backend-1.0-SNAPSHOT.jar
```

服务启动后访问：http://localhost:8080

## 项目结构

```
track-backend/
├── src/main/java/com/track/
│   ├── TrackBackendApplication.java    # 启动类
│   ├── config/
│   │   └── CorsConfig.java             # 跨域配置
│   ├── common/
│   │   └── Result.java                 # 统一响应结果
│   ├── entity/
│   │   ├── User.java                   # 用户实体
│   │   ├── TrackConfig.java            # 埋点配置实体
│   │   └── TrackData.java              # 埋点数据实体
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── TrackConfigRepository.java
│   │   └── TrackDataRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── TrackConfigService.java
│   │   └── TrackDataService.java
│   └── controller/
│       ├── AuthController.java         # 认证接口
│       ├── TrackConfigController.java  # 埋点配置接口
│       └── TrackDataController.java    # 埋点数据接口
├── src/main/resources/
│   ├── application.yml                 # 配置文件
│   └── data/                           # H2数据库文件目录
└── pom.xml
```

## 配置说明

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/trackdb;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console               # H2控制台路径
  jpa:
    hibernate:
      ddl-auto: update                # 自动更新表结构
    show-sql: true                    # 显示SQL
```

## 数据库说明

### H2数据库控制台

访问：http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:file:./data/trackdb;DB_CLOSE_ON_EXIT=FALSE`
- 用户名: `sa`
- 密码: （空）

### 默认账号

系统启动时自动创建默认用户：
- 用户名：`admin`
- 密码：`123456`

## API接口

### 认证接口

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| POST | `/api/auth/login` | `{username, password}` | 登录 |
| POST | `/api/auth/logout` | - | 登出 |
| GET | `/api/auth/userinfo` | - | 获取用户信息 |
| GET | `/api/auth/menus` | - | 获取菜单 |

### 埋点配置接口

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/track-config/list` | eventType, keyword, pageNum, pageSize | 配置列表 |
| GET | `/api/track-config/all` | - | 所有配置（SDK使用） |
| GET | `/api/track-config/detail` | id | 配置详情 |
| POST | `/api/track-config/add` | TrackConfig | 新增配置 |
| POST | `/api/track-config/update` | TrackConfig | 更新配置 |
| POST | `/api/track-config/delete` | `{id}` | 删除配置 |

### 埋点数据接口

| 方法 | 路径 | 参数/请求体 | 说明 |
|------|------|-------------|------|
| POST | `/api/track-data/report` | TrackData | 单条上报 |
| POST | `/api/track-data/batch-report` | `[TrackData]` | 批量上报 |
| GET | `/api/track-data/list` | eventCode, eventType, userId, pageNum, pageSize | 数据列表 |
| GET | `/api/track-data/statistics` | - | 统计数据 |

## 实体定义

### TrackConfig（埋点配置）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| eventName | String | 事件名称 |
| eventCode | String | 事件编码（唯一） |
| eventType | String | 事件类型：page_view/click |
| description | String | 描述 |
| params | String | 参数配置（JSON数组） |
| urlPattern | String | URL正则匹配 |
| status | Integer | 状态：0禁用/1启用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### TrackData（埋点数据）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| eventCode | String | 事件编码 |
| eventType | String | 事件类型 |
| url | String | 页面URL |
| params | String | 上报参数（JSON） |
| userId | String | 用户ID |
| sessionId | String | 会话ID |
| userAgent | String | 用户代理 |
| ip | String | IP地址 |
| duration | Long | 停留时长（ms） |
| eventTime | LocalDateTime | 事件时间 |

## 打包部署

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 后台运行
nohup java -jar track-backend-1.0-SNAPSHOT.jar &
```

## 日志查看

```bash
# 实时查看日志
tail -f nohup.out

# 查看最近100行
tail -100 nohup.out
```
