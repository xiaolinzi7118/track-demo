# RBAC 雪花ID改造联调测试用例

## 1. 测试范围

- 脚本：`20260426_rbac_snowflake_id.sql`
- 后端模块：
  - `UserService.addUser`
  - `RoleService.add`
  - `RoleController.getRoleMenus`
  - `RoleController.updateRoleMenus`
- 接口：
  - `POST /api/user/add`
  - `POST /api/role/add`
  - `GET /api/role/menus`
  - `POST /api/role/update-menus`

---

## 2. 前置条件

1. 数据库已初始化并存在基础 RBAC 数据。
2. 已执行增量脚本 `20260426_rbac_snowflake_id.sql`。
3. 管理端账号具备用户与角色管理权限。

---

## 3. 测试用例

### TC-ID-001 新增角色生成雪花ID
- 步骤：
  1. 调用 `POST /api/role/add` 新增角色。
- 期望：
  - `code=200`
  - 返回 `data.id` 为 `BIGINT` 且不是自增连续值。

### TC-ID-002 新增用户生成雪花ID
- 步骤：
  1. 调用 `POST /api/user/add` 新增用户。
- 期望：
  - `code=200`
  - 返回 `data.id` 为 `BIGINT` 且不是自增连续值。

### TC-ID-003 角色菜单查询返回 menuCode
- 步骤：
  1. 调用 `GET /api/role/menus?id={roleId}`。
- 期望：
  - `code=200`
  - `data` 为对象数组，每项包含 `id` 与 `menuCode`。

### TC-ID-004 角色菜单更新支持字符串ID
- 步骤：
  1. 调用 `POST /api/role/update-menus`，`menuIds` 传字符串数组。
- 期望：
  - `code=200`
  - 角色菜单关系更新成功。

### TC-ID-005 存量数据兼容
- 步骤：
  1. 执行脚本前后分别查询 `sys_user/sys_role/sys_menu` 现有记录数与ID值。
- 期望：
  - 记录数不变；
  - 现有ID值不变；
  - 仅取消了三张表的 `AUTO_INCREMENT` 属性。
