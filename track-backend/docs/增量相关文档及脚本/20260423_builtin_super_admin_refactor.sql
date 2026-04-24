-- 2026-04-23 内置超级管理员改造增量脚本
-- 目标：
-- 1) 增加 sys_user.is_builtin_super_admin 标识
-- 2) 将 admin 账号标记为内置超级管理员
-- 3) 管理员角色与“超级管理员身份”解耦

SET NAMES utf8mb4;

-- 1) sys_user.is_builtin_super_admin
SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD COLUMN is_builtin_super_admin TINYINT NOT NULL DEFAULT 0 COMMENT ''内置超级管理员标识(0-否,1-是)'' AFTER status',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'is_builtin_super_admin'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD INDEX idx_sys_user_builtin_super_admin (is_builtin_super_admin)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'idx_sys_user_builtin_super_admin'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 标记默认 admin 账号为内置超级管理员
UPDATE sys_user
SET is_builtin_super_admin = 1
WHERE username = 'admin';

-- 3) 管理员角色与超级管理员身份解耦
-- 说明：
-- - 本脚本不删除 admin 角色，也不强制重置其菜单权限；
-- - 超级管理员身份由 sys_user.is_builtin_super_admin 决定。
