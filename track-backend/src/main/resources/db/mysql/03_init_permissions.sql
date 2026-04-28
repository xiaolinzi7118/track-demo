START TRANSACTION;

-- 外键关闭场景：先清理孤儿关联数据，避免初始化反复执行后累积脏数据
DELETE ur
FROM sys_user_role ur
LEFT JOIN sys_user u ON ur.user_id = u.id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.id IS NULL OR r.id IS NULL;

DELETE rm
FROM sys_role_menu rm
LEFT JOIN sys_role r ON rm.role_id = r.id
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE r.id IS NULL OR m.id IS NULL;

DELETE sud
FROM sys_user_data_dept sud
LEFT JOIN sys_user u ON sud.user_id = u.id
LEFT JOIN dict_param_item dpi ON sud.dept_id = dpi.id
WHERE u.id IS NULL OR dpi.id IS NULL;

DELETE dpi
FROM dict_param_item dpi
LEFT JOIN dict_param dp ON dpi.param_id = dp.param_id
WHERE dp.param_id IS NULL;

DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
WHERE r.role_code IN ('business', 'developer');

DELETE ur
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
WHERE u.username = 'admin';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'admin'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'business'
  AND m.menu_code IN (
      'dashboard',
      'track',
      'event-manage', 'event-manage:view', 'event-manage:add', 'event-manage:edit', 'event-manage:delete',
      'attribute-manage', 'attribute:view', 'attribute:add', 'attribute:edit', 'attribute:delete',
      'api-interface', 'api-interface:view', 'api-interface:add', 'api-interface:edit', 'api-interface:delete',
      'track-data', 'track-data:view',
      'requirement-manage', 'requirement-manage:view', 'requirement-manage:add', 'requirement-manage:resubmit'
  )
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'developer'
  AND m.menu_code IN (
      'dashboard',
      'track',
      'event-manage', 'event-manage:view',
      'attribute-manage', 'attribute:view',
      'api-interface', 'api-interface:view',
      'track-data', 'track-data:view',
      'requirement-manage', 'requirement-manage:view', 'requirement-manage:status'
  )
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_user_data_dept (user_id, dept_id, create_by, create_time)
SELECT u.id, u.primary_dept_id, 'system', NOW(3)
FROM sys_user u
WHERE u.primary_dept_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    create_by = VALUES(create_by);

COMMIT;
