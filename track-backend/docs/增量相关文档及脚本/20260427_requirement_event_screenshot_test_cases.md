# 20260427 需求管理/事件管理截图功能测试用例

## 1. 数据库变更验证

1. 执行 `20260427_requirement_event_screenshot.sql`。
2. 校验表结构：
- `track_file_asset` 表创建成功。
- `track_requirement.screenshot_file_id` 字段存在。
- `track_config.page_screenshot_file_id` 字段存在。

## 2. 文件上传接口

接口：`POST /api/file/upload-image`

### 用例 2.1 正常上传 JPG
- 前置条件：已登录。
- 入参：`multipart/form-data`，`file` 为 500KB jpg。
- 预期：
- `code=200`。
- 返回 `fileId`。
- `track_file_asset` 新增一条记录，`file_ext=jpg`。

### 用例 2.2 正常上传 PNG
- 入参：`file` 为 1.2MB png。
- 预期：`code=200`，`fileId` 非空，`file_ext=png`。

### 用例 2.3 非法格式
- 入参：`file` 为 gif 或 txt。
- 预期：`code!=200`，提示仅支持 jpg/png。

### 用例 2.4 超过2MB
- 入参：`file` 为 2.5MB png。
- 预期：`code!=200`，提示图片大小不能超过2MB。

## 3. 图片预览接口（安全方案）

接口：`GET /api/file/preview/{fileId}`

### 用例 3.1 已登录预览成功
- 入参：合法 `fileId`。
- 预期：
- HTTP 200。
- `Content-Type` 为 `image/jpeg` 或 `image/png`。
- 响应体为图片二进制。

### 用例 3.2 未登录访问
- 入参：合法 `fileId`。
- 预期：被鉴权拦截，返回未登录错误结构。

### 用例 3.3 fileId不存在
- 入参：不存在的 `fileId`。
- 预期：HTTP 404。

## 4. 需求管理字段链路

### 用例 4.1 新增需求带截图
- 步骤：
- 先上传图片得到 `fileId`。
- 调用 `POST /api/requirement/add`，传 `screenshotFileId`。
- 预期：
- 新增成功。
- `track_requirement.screenshot_file_id` 正确落库。
- `GET /api/requirement/detail` 返回该字段。

### 用例 4.2 编辑重提只修改截图
- 前置条件：需求状态为 `REJECTED`。
- 步骤：仅更新 `screenshotFileId` 后调用 `POST /api/requirement/resubmit`。
- 预期：允许提交，不报“至少修改一个字段”。

## 5. 事件管理字段链路

### 用例 5.1 新增事件带页面截图
- 步骤：上传图片获得 `fileId`，调用 `POST /api/event-manage/add` 传 `pageScreenshotFileId`。
- 预期：新增成功，`track_config.page_screenshot_file_id` 落库。

### 用例 5.2 编辑事件更新页面截图
- 步骤：调用 `POST /api/event-manage/update` 更新 `pageScreenshotFileId`。
- 预期：更新成功，详情/列表返回新值。

## 6. 前端交互验证

1. 需求管理新增页面：可上传/查看/移除需求截图。
2. 需求管理详情页：可查看需求截图；编辑重提页可修改截图。
3. 事件管理新增/编辑弹窗：可上传/查看/移除页面截图。
4. 上传组件格式/大小校验可动态配置；默认仅 jpg/png 且最大2MB。
5. 预览请求通过带 token 的接口拉取 blob，地址栏不暴露免鉴权静态链接。
