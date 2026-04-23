START TRANSACTION;

INSERT INTO sys_role (id, role_code, role_name, description, status, create_time, update_time)
VALUES
    (1, 'admin', '管理员', '系统管理员，拥有全部权限', 1, NOW(3), NOW(3)),
    (2, 'business', '业务人员', '业务配置人员', 1, NOW(3), NOW(3)),
    (3, 'developer', '开发人员', '开发人员', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    description = VALUES(description),
    status = VALUES(status),
    update_time = VALUES(update_time);

INSERT INTO dict_param (param_id, param_name, is_system, status, create_by, create_time, update_by, update_time)
VALUES
    ('SYS_DEPT', '部门', 1, 0, 'system', NOW(3), 'system', NOW(3))
ON DUPLICATE KEY UPDATE
    param_name = VALUES(param_name),
    is_system = VALUES(is_system),
    status = VALUES(status),
    update_by = VALUES(update_by),
    update_time = VALUES(update_time);

INSERT INTO dict_param_item (param_id, item_code, item_name, status, create_by, create_time, update_by, update_time)
SELECT 'SYS_DEPT', 'DEFAULT', '默认部门', 0, 'system', NOW(3), 'system', NOW(3)
WHERE NOT EXISTS (
    SELECT 1
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
);

INSERT INTO sys_user (id, username, password, nickname, avatar, primary_dept_id, status, create_time, update_time)
VALUES
    (1, 'admin', '123456', '管理员', NULL,
        (SELECT id FROM dict_param_item WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0 ORDER BY id ASC LIMIT 1),
        1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    primary_dept_id = VALUES(primary_dept_id),
    status = VALUES(status),
    update_time = VALUES(update_time);

DELETE FROM sys_menu
WHERE menu_code IN (
    'track-data:clear',
    'system-reset-data',
    'system-reset-data:view',
    'system-reset-data:operate'
);

INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, path, icon, sort_order, menu_type, perms, status, create_time, update_time)
VALUES
    (1, 0, '仪表盘', 'dashboard', '/dashboard', 'House', 1, 2, NULL, 1, NOW(3), NOW(3)),

    (2, 0, '埋点管理', 'track', '/track', 'DataAnalysis', 2, 1, NULL, 1, NOW(3), NOW(3)),

    (3, 2, '埋点配置', 'track-config', '/track-config', 'Setting', 1, 2, NULL, 1, NOW(3), NOW(3)),
    (4, 3, '查看', 'track-config:view', NULL, NULL, 1, 3, 'track-config:view', 1, NOW(3), NOW(3)),
    (5, 3, '新增', 'track-config:add', NULL, NULL, 2, 3, 'track-config:add', 1, NOW(3), NOW(3)),
    (6, 3, '编辑', 'track-config:edit', NULL, NULL, 3, 3, 'track-config:edit', 1, NOW(3), NOW(3)),
    (7, 3, '删除', 'track-config:delete', NULL, NULL, 4, 3, 'track-config:delete', 1, NOW(3), NOW(3)),

    (8, 2, '接口来源管理', 'api-interface', '/api-interface', 'Link', 2, 2, NULL, 1, NOW(3), NOW(3)),
    (9, 8, '查看', 'api-interface:view', NULL, NULL, 1, 3, 'api-interface:view', 1, NOW(3), NOW(3)),
    (10, 8, '新增', 'api-interface:add', NULL, NULL, 2, 3, 'api-interface:add', 1, NOW(3), NOW(3)),
    (11, 8, '编辑', 'api-interface:edit', NULL, NULL, 3, 3, 'api-interface:edit', 1, NOW(3), NOW(3)),
    (12, 8, '删除', 'api-interface:delete', NULL, NULL, 4, 3, 'api-interface:delete', 1, NOW(3), NOW(3)),

    (13, 2, '数据回检', 'track-data', '/track-data', 'DataLine', 3, 2, NULL, 1, NOW(3), NOW(3)),
    (14, 13, '查看', 'track-data:view', NULL, NULL, 1, 3, 'track-data:view', 1, NOW(3), NOW(3)),

    (15, 0, '系统管理', 'system', '/system', 'Tools', 3, 1, NULL, 1, NOW(3), NOW(3)),

    (16, 15, '用户管理', 'system-user', '/system/user', 'User', 1, 2, NULL, 1, NOW(3), NOW(3)),
    (17, 16, '查看', 'system-user:view', NULL, NULL, 1, 3, 'system-user:view', 1, NOW(3), NOW(3)),
    (18, 16, '新增', 'system-user:add', NULL, NULL, 2, 3, 'system-user:add', 1, NOW(3), NOW(3)),
    (19, 16, '编辑', 'system-user:edit', NULL, NULL, 3, 3, 'system-user:edit', 1, NOW(3), NOW(3)),
    (20, 16, '删除', 'system-user:delete', NULL, NULL, 4, 3, 'system-user:delete', 1, NOW(3), NOW(3)),

    (21, 15, '角色管理', 'system-role', '/system/role', 'Lock', 2, 2, NULL, 1, NOW(3), NOW(3)),
    (22, 21, '查看', 'system-role:view', NULL, NULL, 1, 3, 'system-role:view', 1, NOW(3), NOW(3)),
    (23, 21, '新增', 'system-role:add', NULL, NULL, 2, 3, 'system-role:add', 1, NOW(3), NOW(3)),
    (24, 21, '编辑', 'system-role:edit', NULL, NULL, 3, 3, 'system-role:edit', 1, NOW(3), NOW(3)),
    (25, 21, '删除', 'system-role:delete', NULL, NULL, 4, 3, 'system-role:delete', 1, NOW(3), NOW(3)),
    (26, 15, '参数维护', 'system-dict-param', '/system/dict-param', 'Operation', 3, 2, NULL, 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    menu_type = VALUES(menu_type),
    perms = VALUES(perms),
    status = VALUES(status),
    update_time = VALUES(update_time);

COMMIT;
