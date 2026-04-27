# 事件管理/属性管理改造联调测试用例

## 1. 测试范围

- 脚本：`20260427_event_attribute_manage.sql`
- 后端接口：
  - `GET /api/event-manage/list`
  - `GET /api/event-manage/requirement-options`
  - `POST /api/event-manage/add`
  - `POST /api/event-manage/update`
  - `GET /api/event-manage/all`
  - `GET /api/attribute/list`
  - `POST /api/attribute/add`
  - `POST /api/attribute/update`
  - `POST /api/attribute/delete`

---

## 2. 前置条件

1. 已执行 `01_schema.sql + 02_init_data.sql + 03_init_permissions.sql`。  
2. 已执行增量脚本 `20260427_event_attribute_manage.sql`。  
3. 存在状态为 `SCHEDULING` 或 `DEVELOPING` 的需求数据。  
4. 接口来源管理中至少存在一条接口数据。  

---

## 3. 测试用例

### TC-EA-001 菜单与权限加载
- 步骤：
  1. 使用 `business` 用户登录；
  2. 拉取 `GET /api/menu/user-menus` 与 `GET /api/menu/user-permissions`。
- 期望：
  - 菜单中存在 `event-manage` 与 `attribute-manage`；
  - 权限中存在 `event-manage:*`、`attribute:*`。

### TC-EA-002 新增属性（custom + api_data）
- 步骤：
  1. 调用 `POST /api/attribute/add`，`attributeType=custom`、`sourceType=api_data`、传 `interfaceId`。
- 期望：
  - `code=200`；
  - 返回 `attributeId` 以 `ATTR` 开头；
  - 返回 `interfacePath` 自动补齐。

### TC-EA-003 属性唯一性校验
- 步骤：
  1. 在同一 `attributeType` 下重复新增相同 `attributeName`。
- 期望：
  - 返回错误；
  - 错误信息包含“同类型下属性名称已存在”。

### TC-EA-004 被引用属性不可删除
- 步骤：
  1. 创建事件并关联某属性；
  2. 删除该属性。
- 期望：
  - 删除失败；
  - 错误信息为“该属性已被事件引用，无法删除”。

### TC-EA-005 新增事件要求关联需求
- 步骤：
  1. 调用 `POST /api/event-manage/add`，不传 `requirementId`。
- 期望：
  - 返回错误；
  - 错误信息包含“关联需求不能为空”。

### TC-EA-006 需求状态校验
- 步骤：
  1. 选择状态非 `SCHEDULING/DEVELOPING` 的需求创建事件。
- 期望：
  - 返回错误；
  - 错误信息包含“仅可关联排期中或开发中的需求”。

### TC-EA-007 page_view 来源限制
- 步骤：
  1. 创建 `eventType=page_view` 事件；
  2. params 里放 `sourceType=node_content` 或 `api_data` 的 custom 属性。
- 期望：
  - 返回错误；
  - 错误信息提示页面曝光不支持这两类来源。

### TC-EA-008 事件唯一性校验
- 步骤：
  1. 新增启用事件A：`eventCode=E100`，`urlPattern=/home.*`；
  2. 再新增启用事件B同样组合。
- 期望：
  - 第二次新增失败；
  - 错误信息包含“事件编码与生效页面URL组合在启用数据中必须唯一”。

### TC-EA-009 SDK 拉取配置
- 步骤：
  1. 未登录直接请求 `GET /api/event-manage/all`。
- 期望：
  - 可成功返回；
  - 仅返回 `status=1` 事件；
  - 返回结果包含完整 `params`。

### TC-EA-010 引用接口保护删除
- 步骤：
  1. 某接口被属性或事件 `api_data` 引用；
  2. 调用 `POST /api/api-interface/delete` 删除。
- 期望：
  - 删除失败；
  - 错误信息提示接口被引用。
