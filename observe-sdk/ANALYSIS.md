# observe-sdk 代码深度分析

## 一、项目概览

observe-sdk 是一个面向 H5 页面的 **前端性能监控 SDK**，基于浏览器原生 `PerformanceObserver` API 采集 Web Vitals 核心指标，并将数据上报至移动网关（MGS）。主要应用于某银行 App 内嵌 H5 页面的性能监控场景（通过 mPaaS/AlipayJSBridge 容器）。

### 文件结构

| 文件 | 行数 | 职责 |
|------|------|------|
| [observe-insert.js](observe-insert.js) | 27 | 入口文件，配置初始化 + 启动监控与提交 |
| [perfm-trigger.js](perfm-trigger.js) | 534 | 核心逻辑，含4个类：数据结构、基类、指标采集、数据提交 |

### 类图关系

```
DataInfo (静态数据结构)
    │
BaseMonitor (基类：配置读取、日志、缓存Key)
    ├── ObserveTrigger (指标采集：PerformanceObserver 挂载与回调)
    └── ObservePoster  (数据提交：轮询读取缓存 → 获取设备信息 → RPC上报)
```

---

## 二、入口文件分析 — observe-insert.js

```js
window.wlbOpacteam = {
  expo: { isOn, logOn, scanInteval: 3000 },   // 曝光埋点（当前未使用）
  perfm: { isOn, logOn, pageId, obDuration: 1, debounceMs: 7000 }  // 性能监控配置
}
```

### 配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `isOn` | true | 总开关 |
| `logOn` | true | 控制台日志开关 |
| `pageId` | 'MixinPage' | 页面标识，用于服务端区分页面来源 |
| `obDuration` | 1（分钟） | 监控持续时间上限，到期后自动断开所有 Observer |
| `debounceMs` | 7000（毫秒） | 轮询间隔，同时作为提交防抖周期 |

入口依次创建：
1. `ObserveTrigger` 实例 → 调用 `startObserve()` 开始采集
2. `ObservePoster` 实例 → 构造函数中自动启动轮询提交

> **注意**：`ObservePoster` 创建后并未调用任何方法，但其 `init()` 在构造函数中已执行，会自动启动轮询。`genUID()` 和 `testUID()` 是测试代码。

---

## 三、核心类详解 — perfm-trigger.js

### 3.1 DataInfo — 数据结构定义

定义了上报数据的完整字段模板：

```
┌─────────────────────────────────────────────────────────┐
│                   Performance Data                       │
├──────────────┬──────────────────────────────────────────┤
│ 核心指标      │ cls, fp, fcp, domload, dns, tcp,        │
│              │ fbyte, lcp, lcpurl, fid                  │
├──────────────┼──────────────────────────────────────────┤
│ 资源大小      │ encodedSize (解码后), transferSize (传输)  │
├──────────────┼──────────────────────────────────────────┤
│ 设备信息      │ deviceType (1安卓/2iOS/0其他),            │
│              │ deviceBrand, deviceSpec, appVersion       │
├──────────────┼──────────────────────────────────────────┤
│ 网络信息      │ networkType, offlinepkgVersion           │
├──────────────┼─────────────────────────────────────────┤
│ 页面信息      │ pageId, clientUrl, clientAgent,         │
│              │ clientNavi, clientTime, clientUid        │
├──────────────┼──────────────────────────────────────────┤
│ 用户标识      │ deviceId, clientIp, clientPos            │
└──────────────┴──────────────────────────────────────────┘
```

### 3.2 BaseMonitor — 监控基类

**职责**：配置解析、日志控制、环境判断

**关键逻辑**：

```js
this.inApp = navigator.userAgent.includes('mPaaS')  // 判断是否在App容器内
```

- 通过 UA 中是否包含 `mPaaS` 判断运行环境
- 读取 `window.wlbOpacteam.perfm` 配置，缺失字段有兜底默认值
- 配置校验不通过时设置 `isOptionChkPass = false`（但后续未使用此标记做拦截）

**缓存 Key**：
- `wlb-perfm-cache`：存储采集到的性能数据（JSON格式）
- `wlbobs`：存储本地日志文本（逗号分隔）

### 3.3 ObserveTrigger — 指标采集类

这是整个 SDK 的核心，通过 `PerformanceObserver` 采集浏览器性能指标。

#### 采集的指标及其含义

| 指标 | 全称 | 采集方式 | 衡量内容 |
|------|------|---------|---------|
| **FP** | First Paint | `entryTypes: ['paint']` | 首次绘制时间（白屏结束） |
| **FCP** | First Contentful Paint | `entryTypes: ['paint']` | 首次内容绘制时间 |
| **LCP** | Largest Contentful Paint | `entryTypes: ['largest-contentful-paint']` | 最大内容元素渲染时间 |
| **CLS** | Cumulative Layout Shift | `entryTypes: ['layout-shift']` | 累积布局偏移量 |
| **FID** | First Input Delay | `entryTypes: ['first-input']` | 首次输入延迟 |
| **DNS** | DNS Lookup | `navigation` 条目 | DNS 查询耗时 |
| **TCP** | TCP Connect | `navigation` 条目 | TCP 连接耗时 |
| **FByte** | First Byte | `navigation` 条目 | 首字节到达时间（TTFB） |
| **DOM Load** | DOM Load | `navigation` 条目 | 页面完全加载时间 |

#### 平台差异策略 — iOS vs Android

```
                    iOS                              Android/其他
                    │                                    │
     ┌──────────────┴──────────────┐     ┌───────────────┴───────────────┐
     │  轮询策略：每次只注册1个      │     │  同时注册4个 Observer：        │
     │  Observer（localStorage     │     │  obsMain (navi + paint)       │
     │  记录轮询索引）              │     │  obsRes  (resource + lcp)     │
     │  scheduleList: ['navi']     │     │  obsCls  (layout-shift) ←缺失 │
     │  当前只执行 navi 分支        │     │  obsFid  (first-input)  ←缺失 │
     └─────────────────────────────┘     └───────────────────────────────┘
```

**iOS 策略**：出于性能考量，每次页面加载只注册一种 Observer，通过 `localStorage` 的 `perfm-type` 索引做轮询。但当前 `scheduleList` 只有 `['navi']`，意味着 iOS 上只会采集 navigation + paint 指标，**不会采集 LCP、CLS、FID**。

**Android 策略**：代码中尝试注册 `obsRes`（含 resource 和 lcp）和 `obsMain`（含 navigation 和 paint），但 **obsCls 和 obsFid 未在 Android 分支中注册**，仅创建了实例但未调用 `.observe()`。

#### 四个 Observer Handler 详解

**1. mainHandler** — 主处理器
```
输入: entryTypes = ['navigation', 'paint']
         │
    ┌────┴─────┐
    ▼          ▼
 navigation   paint entries
    │          ├─ 'first-paint' → fp
    │          └─ 'first-contentful-paint' → fcp
    ├─ domload = loadEventEnd - startTime
    ├─ dns = domainLookupEnd - domainLookupStart
    ├─ tcp = connectEnd - connectStart
    └─ fbyte = responseStart - requestStart
```

**2. resHandler** — 资源处理器
```
输入: entryTypes = ['resource', 'largest-contentful-paint']
         │
         ▼
    largest-contentful-paint entries
         ├─ lcp = startTime
         └─ lcpurl = url
```

**3. clsHandler** — 布局偏移处理器
```
输入: entryTypes = ['layout-shift']
         │
         ▼
    过滤非用户输入导致的偏移 (hadRecentInput === false)
         └─ cls 累加 value
```

**4. fidHandler** — 首次输入延迟处理器
```
输入: entryTypes = ['first-input']
         │
         ▼
    first-input entryType
         └─ fid = processingEnd - processingStart
```

#### 数据流转过程

```
PerformanceObserver 回调
        │
        ▼
   各 Handler 处理
        │
        ▼
   this.perfmData 赋值
        │
        ▼
   saveLocal() → localStorage['wlb-perfm-cache'] = JSON(perfmData)
        │
        ▼
   ObservePoster 轮询读取 localStorage
        │
        ▼
   补充设备信息 → RPC 上报
```

### 3.4 ObservePoster — 数据提交类

**职责**：定时轮询 localStorage 缓存 → 补充设备信息 → 通过 JSBridge RPC 上报

#### 核心流程

```
init()
  │
  └─ setInterval(debounceMs)
        │
        ├─ 检查是否超过 obDuration → 超时则 disconnect 所有 Observer 并 clearInterval
        │
        └─ commit()
              │
              ├─ 比对 lastCacheStr，无变化则跳过
              │
              ├─ 解析 perfmCache → 补充 clientUid / clientTime / pageId
              │
              ├─ getDeviceData() → 通过 AlipayJSBridge 获取设备信息
              │     ├─ deviceId (virtualDeviceId)
              │     ├─ clientIp
              │     ├─ appVersion
              │     ├─ deviceBrand (phoneModel)
              │     ├─ deviceSpec (deviceTypeNo)
              │     ├─ offlinepkgVersion
              │     └─ networkType
              │
              └─ requestRPC(perfmData)
                    │
                    └─ AlipayJSBridge.call('rpc', MGSAPI, ...)
                          │
                          ├─ 成功 (error === 'SUC0000') → 结束
                          └─ 失败 → 更新 localStorage 缓存，下次轮询重试
```

#### UID 生成策略

```js
genUID() {
  const timePart = Date.now().toString(36)      // 时间戳 → base36
  const randomPart = Math.random().toString(36)  // 随机数 → base36
  return (timePart + randomPart).replace('.', '').toUpperCase()
}
```

- 时间戳 base36 编码保证时序性
- 随机数保证唯一性
- `testUID()` 测试 10 万次生成的 UID 是否有重复（通过 Map size 验证）

#### 防抖机制

`debounce()` 方法支持立即/延迟两种模式，但在当前代码中 **未被调用**。提交频率完全由 `setInterval(debounceMs)` 控制。

#### 提交失败重试

上报失败时不会立即重试，而是更新 localStorage 中的缓存（附加 `commitFailCount`），等待下次轮询检测到缓存变化后重新提交。

---

## 四、关键设计特点

### 4.1 优点

1. **标准化指标采集**：覆盖 Google Core Web Vitals 核心指标（FP/FCP/LCP/CLS/FID）及网络耗时指标
2. **平台差异化处理**：iOS/Android 分流，iOS 采用轮询策略避免同时注册多个 Observer 造成性能问题
3. **渐进式采集**：通过 localStorage 做中间缓存，Observer 和 Poster 解耦
4. **超时自动清理**：达到 `obDuration` 后主动 disconnect 所有 Observer，避免持续占用资源
5. **提交去重**：通过 `lastCacheStr` 比对避免重复上报相同数据
6. **失败重试**：RPC 上报失败后通过缓存标记实现延迟重试

### 4.2 潜在问题与改进建议

| # | 问题 | 严重程度 | 说明 |
|---|------|---------|------|
| 1 | **iOS 仅采集 navi 指标** | 高 | `scheduleList` 只有 `['navi']`，LCP/CLS/FID 在 iOS 上永远无法采集 |
| 2 | **Android 缺少 CLS/FID 注册** | 高 | `obsCls` 和 `obsFid` 实例未在 Android 分支中调用 `.observe()` |
| 3 | **CLS/FID 在 Android 上未创建实例** | 高 | `startObserve()` 的 else 分支只创建了 `obsMain` 和 `obsRes`，未创建 `obsCls` 和 `obsFid` |
| 4 | **logLocal 日志无清理机制** | 低 | `wlbobs` key 不断追加字符串，无上限控制，长期可能撑爆 localStorage |
| 5 | **debounce 方法未使用** | 低 | 定义了防抖函数但未被调用，属于冗余代码 |
| 6 | **commit 中 JSON.parse 无容错** | 中 | `perfmCache` 为空字符串时 `JSON.parse('')` 会抛异常 |
| 7 | **getDeviceData 的异常处理不完整** | 中 | catch 中只设置了 `deviceId`，但后续访问 `deviceRes.data.phoneModel` 等字段会报错 |
| 8 | **commitFailCount 写入位置不一致** | 低 | 失败时写入的是解析后的对象，但 `perfmData` 结构中没有此字段 |
| 9 | **isOptionChkPass 未实际使用** | 低 | 基类设置了此标记但子类未检查 |
| 10 | **Observer 引用挂在 window/context 上** | 低 | 使用全局变量持有 Observer 实例，可能与其他代码冲突 |

### 4.3 数据生命周期

```
页面加载
  │
  ├─ 0ms: ObserveTrigger 创建，清空 wlb-perfm-cache
  │
  ├─ ~0ms: PerformanceObserver 开始监听
  │
  ├─ ~100ms: FP 触发 → perfmData.fp 写入
  │
  ├─ ~200ms: FCP 触发 → perfmData.fcp 写入
  │
  ├─ ~500ms: LCP 触发 → perfmData.lcp / lcpurl 写入
  │
  ├─ ~1000ms: Navigation 完成 → domload/dns/tcp/fbyte 写入
  │
  ├─ ~7000ms: 首次轮询 → commit() → 读取缓存 → 补充设备信息 → RPC 上报
  │
  ├─ ~14000ms: 第二次轮询 → 检测数据无变化 → 跳过
  │
  ├─ ...
  │
  └─ 60000ms (1分钟): 达到 obDuration → disconnect 所有 Observer → 停止轮询
```

### 4.4 浏览器兼容性

| API | Chrome | Safari | 备注 |
|-----|--------|--------|------|
| PerformanceObserver | 52+ | 11+ | iOS 11+ 支持 |
| entryTypes: 'navigation' | 57+ | 11+ | 完整支持 |
| entryTypes: 'paint' | 60+ | 不支持 | **Safari 不支持 paint 类型**，iOS 上 FP/FCP 无法采集 |
| entryTypes: 'largest-contentful-paint' | 77+ | 不支持 | **Safari 不支持 LCP** |
| entryTypes: 'layout-shift' | 77+ | 不支持 | **Safari 不支持 CLS** |
| entryTypes: 'first-input' | 77+ | 不支持 | **Safari 不支持 FID**（但支持 Event Timing API） |

> **关键发现**：在 iOS/Safari 环境下，仅有 `navigation` 类型的指标可以正常采集。paint、LCP、CLS、FID 在 Safari 上均不支持，这与代码中 iOS 只注册 navi 的策略吻合——可能是开发者已知晓此兼容性问题而做出的取舍。

---

## 五、总结

observe-sdk 是一个针对银行 App 内嵌 H5 页面的轻量级性能监控方案，核心思路是：

1. **采集层**：利用 `PerformanceObserver` 被动监听浏览器性能条目，覆盖 Web Vitals 核心指标
2. **缓存层**：通过 `localStorage` 解耦采集与提交
3. **提交层**：定时轮询 + JSBridge RPC 上报，支持失败重试

整体架构清晰，但在 **指标覆盖完整性**（iOS/Android 均存在部分指标未注册）、**错误容错**（JSON.parse、设备信息解析）和 **资源管理**（localStorage 无上限）方面存在改进空间。特别是在非 App 环境（浏览器直接访问）下，所有设备相关字段都会缺失，仅上报性能指标本身。
