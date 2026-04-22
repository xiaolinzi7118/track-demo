# Track Backend MySQL 表结构设计与初始化说明

## 1. 目标

- 将 `track-backend` 从 H2 内嵌数据库切换为 MySQL。
- 不迁移历史 H2 数据，采用全量初始化。
- 初始化内容包含：管理员账号、角色、菜单按钮、角色权限关系。
- 移除“重置数据”功能相关菜单与接口。

## 2. 数据库与字符集建议

- MySQL: 8.0+
- Charset: `utf8mb4`
- Collation: `utf8mb4_unicode_ci`
- 时区: `Asia/Shanghai`

## 3. 表清单

- `sys_user` 用户表
- `sys_role` 角色表
- `sys_menu` 菜单/按钮权限表
- `sys_user_role` 用户-角色关联表
- `sys_role_menu` 角色-菜单关联表
- `track_config` 埋点配置表
- `track_data` 埋点上报数据表
- `api_interface` 接口来源表
- `dict_param` 参数主表
- `dict_param_item` 参数项表
- `dict_id_sequence` 参数业务ID序列表

## 4. 关键设计点

- 主键策略：`BIGINT AUTO_INCREMENT`（兼容 JPA `GenerationType.IDENTITY`）。
- RBAC 约束：
  - `sys_user_role` 增加唯一键 `(user_id, role_id)`。
  - `sys_role_menu` 增加唯一键 `(role_id, menu_id)`。
- RBAC 外键：
  - 关联表对主表采用 `ON DELETE CASCADE`。
- 菜单结构：
  - `menu_type`: `1=目录`, `2=页面`, `3=按钮`。
  - 按钮权限标识通过 `perms` 字段维护，如 `system-role:edit`。
- 参数维护：
  - 主表软删除字段：`status`，`0=生效`，`1=已删除`。
  - 参数名称唯一性仅对 `status=0` 生效，通过 `active_name` 生成列唯一索引保证。
  - 参数业务ID格式：`DICT + yyyyMMdd + 8位序号`，序号由 `dict_id_sequence` 并发安全生成。
  - 参数维护接口采用后端角色硬校验，仅允许 `admin/developer`。
- 重置数据能力：
  - 已从初始化菜单与权限中移除 `system-reset-data*`。

## 5. 初始化数据策略

### 5.1 账号与角色

- 默认账号：`admin / 123456`
- 角色：`admin`、`business`、`developer`

### 5.2 菜单与按钮（26 条）

包含以下业务域：

- 仪表盘
- 埋点管理
  - 埋点配置（查看/新增/编辑/删除）
  - 接口来源管理（查看/新增/编辑/删除）
  - 数据回检（查看）
- 系统管理
  - 用户管理（查看/新增/编辑/删除）
  - 角色管理（查看/新增/编辑/删除）
  - 参数维护（页面菜单，权限由后端角色硬校验）

### 5.3 权限初始化

- `admin`：全菜单全权限。
- `business`：仪表盘 + 埋点配置（全）+ 接口来源（全）+ 数据回检（查看）。
- `developer`：仪表盘 + 埋点配置（查看）+ 接口来源（查看）+ 数据回检（查看）。
- `admin` 用户自动绑定 `admin` 角色。

## 6. 脚本清单

位于 `src/main/resources/db/mysql`：

1. `01_schema.sql`：建表脚本（DDL）
2. `02_init_data.sql`：基础初始化数据（用户/角色/菜单）
3. `03_init_permissions.sql`：权限初始化（用户角色、角色菜单）

## 7. 执行顺序

1. 执行 `01_schema.sql`
2. 执行 `02_init_data.sql`
3. 执行 `03_init_permissions.sql`

应用已配置为启动自动执行以上脚本。
