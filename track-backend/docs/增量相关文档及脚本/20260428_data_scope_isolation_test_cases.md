# 2026-04-28 数据权限隔离修复测试用例

## 一、测试准备

- 部门：A、B、X（X 作为开发团队部门，`extraAttr=开发`）。
- 用户：
  - 用户A：主部门A，数据权限仅A，角色=业务人员。
  - 用户B：主部门B，数据权限仅B，角色=业务人员。
  - 用户D：主部门X，角色=developer（非admin）。
- 测试数据：
  - 需求RA：提出人=用户A，`devTeamDeptId=X`。
  - 需求RB：提出人=用户B，`devTeamDeptId=X`。
  - 事件EA：关联RA。
  - 事件EB：关联RB。
  - 回检数据：分别上报EA、EB事件数据。

## 二、需求管理

1. 用户A访问 `/api/requirement/list`。
- 期望：仅返回RA，不返回RB。

2. 用户B访问 `/api/requirement/list`。
- 期望：仅返回RB，不返回RA。

3. 用户A用RB的 `requirementId` 访问 `/api/requirement/detail`。
- 期望：返回无权限错误。

4. 用户A对RB调用 `/api/requirement/status-change`。
- 期望：返回无权限错误。

## 三、仪表盘

1. 用户A访问 `/api/requirement/dashboard-statistics` 与 `/api/requirement/dashboard-trend`。
- 期望：统计仅来自RA，不包含RB。

2. 用户B同样访问。
- 期望：统计仅来自RB，不包含RA。

## 四、事件管理

1. 用户A访问 `/api/event-manage/list`。
- 期望：仅返回EA，不返回EB。

2. 用户A访问 `/api/event-manage/requirement-options`。
- 期望：仅返回自己可见需求（RA）。

3. 用户A尝试新增事件关联RB。
- 期望：后端拦截并返回无权限错误。

4. 用户A访问EB详情或编辑EB。
- 期望：返回无权限错误。

## 五、数据回检

1. 用户A访问 `/api/track-data/list`。
- 期望：仅返回EA对应数据，不返回EB对应数据。

2. 用户A访问EB对应回检ID详情。
- 期望：返回无权限错误。

## 六、开发角色可见性

1. 用户D访问 `/api/requirement/list`。
- 期望：仅返回 `devTeamDeptId = X` 的需求（RA、RB），不受提出人主部门A/B影响。

2. 用户D访问事件与回检列表。
- 期望：仅返回关联到上述可见需求的事件和回检数据。

