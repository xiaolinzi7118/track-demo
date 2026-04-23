START TRANSACTION;

DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
WHERE r.role_code IN ('admin', 'business', 'developer');

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
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'business'
  AND m.menu_code IN (
      'dashboard',
      'track',
      'track-config', 'track-config:view', 'track-config:add', 'track-config:edit', 'track-config:delete',
      'api-interface', 'api-interface:view', 'api-interface:add', 'api-interface:edit', 'api-interface:delete',
      'track-data', 'track-data:view'
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
      'track-config', 'track-config:view',
      'api-interface', 'api-interface:view',
      'track-data', 'track-data:view'
  )
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_user_data_dept (user_id, dept_id, create_by, create_time)
SELECT u.id, u.primary_dept_id, 'system', NOW(3)
FROM sys_user u
WHERE u.primary_dept_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    create_by = VALUES(create_by);

COMMIT;
