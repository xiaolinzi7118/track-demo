# 2026-04-27 需求管理开发团队部门化改造测试用例

## 1. 前置准备

1. 执行增量脚本：`20260427_requirement_dev_team_dept.sql`
2. 在参数维护中准备部门数据（`paramId=SYS_DEPT`）：
   - 至少 1 条 `status=0` 且 `extraAttr=开发`
   - 至少 1 条 `status=0` 且 `extraAttr!=开发`
3. 准备角色与账号：
   - 开发人员A：`developer` 角色，主部门=开发部门X
   - 开发人员B：`developer` 角色，主部门=开发部门Y（与X不同）
   - 业务人员C：`business` 角色，主部门任意

## 2. 参数维护

### TC-DICT-001 参数项附加属性维护
- 步骤：在参数维护新增/编辑参数项，填写 `extraAttr`
- 预期：保存成功，详情接口返回 `items[].extraAttr`

## 3. 用户管理

### TC-USER-001 开发角色主部门限制-通过
- 步骤：给用户分配 `developer` 角色，主部门选择 `extraAttr=开发` 的部门
- 预期：保存成功

### TC-USER-002 开发角色主部门限制-拒绝
- 步骤：给用户分配 `developer` 角色，主部门选择 `extraAttr!=开发` 的部门
- 预期：保存失败，提示主部门必须为开发属性部门

### TC-USER-003 非开发角色主部门限制
- 步骤：仅分配非 `developer` 角色，主部门选择任意部门
- 预期：保存成功

## 4. 需求管理

### TC-REQ-001 负责开发团队来源校验
- 步骤：创建需求时选择负责开发团队
- 预期：仅可选 `SYS_DEPT` 中 `status=0 且 extraAttr=开发` 的部门项

### TC-REQ-002 开发同部门可变更状态
- 步骤：开发人员A操作其部门X对应需求执行状态变更
- 预期：成功，返回最新状态

### TC-REQ-003 开发跨部门不可变更状态
- 步骤：开发人员B尝试变更部门X对应需求状态
- 预期：失败，提示无权限进行该状态流转

### TC-REQ-004 非开发角色不可变更状态
- 步骤：业务人员C调用 `/api/requirement/status-change`
- 预期：失败（无状态变更权限或无可用流转）

## 5. DEFAULT默认部门清理

### TC-DEPT-001 默认部门项删除验证
- 步骤：查询 `dict_param_item` 中 `param_id='SYS_DEPT' AND item_code='DEFAULT'`
- 预期：该项 `status=1`（已软删除）

