# 需求管理联调测试用例清单

## 1. 测试范围

- 模块：需求管理
- 接口：
  - `/api/requirement/list`
  - `/api/requirement/detail`
  - `/api/requirement/add`
  - `/api/requirement/status-change`
  - `/api/requirement/resubmit`
- 角色：
  - 管理员（`admin`）
  - 开发人员（`developer`）
  - 业务人员（`business`）

---

## 2. 前置条件

1. 已执行数据库脚本：
   - `01_schema.sql`
   - `02_init_data.sql`
   - `03_init_permissions.sql`
   - 或增量脚本 `20260424_requirement_manage.sql`
2. 字典中存在可用项：
   - 业务线参数：`DICT2026042200000001`
   - 开发团队参数：`DICT2026042200000002`
   - 至少各 1 条 `status=0` 的字典项。
3. 存在三类测试账号并可登录。

---

## 3. 接口联调用例

### 3.1 新增需求

#### TC-REQ-001 新增成功（业务人员）
- 前置：业务人员已登录，具备 `requirement-manage:add`
- 步骤：
  1. 调用 `/api/requirement/add` 提交完整必填字段
- 期望：
  - `code=200`
  - 返回 `status=PENDING_REVIEW`
  - `requirementId` 非空
  - 数据库 `track_requirement` 记录写入
  - `track_log` 新增 `CREATE` 日志，`log_type=requirement_manage`

#### TC-REQ-002 新增失败-标题缺失
- 步骤：`title` 传空
- 期望：`code!=200`，提示标题必填

#### TC-REQ-003 新增失败-字典编码非法
- 步骤：`businessLineCode` 或 `devTeamCode` 传不存在值
- 期望：`code!=200`，提示字典项无效

---

### 3.2 列表与详情

#### TC-REQ-004 列表分页筛选
- 步骤：
  1. 调用 `/api/requirement/list?pageNum=1&pageSize=10`
  2. 带 `title/statusList/priority` 组合筛选
- 期望：
  - 返回分页结构（`content/totalElements`）
  - 筛选结果符合条件

#### TC-REQ-005 列表排序
- 步骤：
  1. `sortField=createTime&sortOrder=desc`
  2. `sortField=updateTime&sortOrder=asc`
  3. `sortField=priority&sortOrder=asc`
- 期望：返回顺序符合排序规则

#### TC-REQ-006 详情查询成功
- 步骤：按 `requirementId` 调用详情接口
- 期望：
  - 返回需求完整字段
  - 包含 `availableActions`
  - 包含 `logs[]`

#### TC-REQ-007 详情查询失败-ID不存在
- 步骤：传不存在的 `requirementId`
- 期望：`code!=200`，提示需求不存在

---

### 3.3 状态流转与权限

#### TC-REQ-008 管理员待审核->排期中
- 前置：存在 `PENDING_REVIEW` 需求
- 步骤：管理员调用状态变更，`targetStatus=SCHEDULING`
- 期望：成功，状态更新，日志写入 `STATUS_CHANGE`

#### TC-REQ-009 管理员待审核->审核不通过（有原因）
- 步骤：`targetStatus=REJECTED` 且 `opinion` 非空
- 期望：成功，日志 `opinion` 入库

#### TC-REQ-010 管理员待审核->审核不通过（无原因）
- 步骤：`targetStatus=REJECTED` 且不传 `opinion`
- 期望：失败，提示原因必填

#### TC-REQ-011 开发人员排期中->开发中
- 前置：需求状态 `SCHEDULING`
- 步骤：开发人员调用变更到 `DEVELOPING`
- 期望：成功

#### TC-REQ-012 开发人员排期中->待审核（越权）
- 步骤：开发人员尝试 `SCHEDULING -> PENDING_REVIEW`
- 期望：失败（无权限）

#### TC-REQ-013 业务人员尝试状态变更（越权）
- 步骤：业务人员调用 `/status-change`
- 期望：失败（无权限）

#### TC-REQ-014 非法跳转校验
- 步骤：管理员或开发尝试非法跳转（如 `PENDING_REVIEW -> ONLINE`）
- 期望：失败（状态流转非法）

---

### 3.4 REJECTED 编辑重提

#### TC-REQ-015 提出人重提成功
- 前置：需求状态 `REJECTED`，当前用户是提出人
- 步骤：调用 `/resubmit`，至少修改一个字段
- 期望：
  - 成功
  - 状态变更为 `PENDING_REVIEW`
  - `updateTime` 更新
  - 日志写入 `EDIT_RESUBMIT`

#### TC-REQ-016 重提失败-无字段变化
- 步骤：提交与原值完全一致
- 期望：失败，提示至少修改一个字段

#### TC-REQ-017 重提失败-非提出人且非管理员
- 步骤：其他业务人员尝试重提
- 期望：失败（无权限）

#### TC-REQ-018 重提失败-状态非REJECTED
- 步骤：对 `PENDING_REVIEW` 或其他状态调用重提
- 期望：失败

---

## 4. 前端联调验证点

1. 菜单可见性  
- `business`、`developer` 可见“需求管理”菜单，路径 `/requirement-manage`。

2. 列表操作按钮动态渲染  
- 按后端 `availableActions` 渲染；前端不硬编码权限判断。

3. 审核不通过原因录入  
- 点击变更为“审核不通过”必须输入原因，且能在详情日志展示。

4. 重提变更校验  
- 前端拦截“未修改即提交”，后端也二次校验。

5. 刷新策略  
- 新增/状态变更/重提成功后列表自动刷新。

---

## 5. 数据库核对 SQL（建议）

```sql
-- 查看需求主表
SELECT requirement_id, title, status, priority, proposer_name, create_time, update_time
FROM track_requirement
ORDER BY create_time DESC
LIMIT 20;

-- 查看需求日志
SELECT log_type, requirement_id, action_type, from_status, to_status, opinion, operator_name, operate_time
FROM track_log
WHERE log_type = 'requirement_manage'
ORDER BY operate_time DESC
LIMIT 50;
```

---

## 6. 回归重点

1. 现有埋点配置/接口来源/数据回检功能不受影响。  
2. 登录、菜单加载、权限指令 `v-permission` 行为正常。  
3. 字典接口 `/dict-param/ids-list` 多 `paramIds` 请求兼容正常。  

