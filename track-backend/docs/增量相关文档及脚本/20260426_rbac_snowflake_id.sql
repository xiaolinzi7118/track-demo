-- 2026-04-26 RBAC 主键策略改造：用户/角色/菜单改为应用侧雪花ID
-- 目标：
-- 1) 保留现有数据ID不变
-- 2) 取消 sys_user/sys_role/sys_menu 的 AUTO_INCREMENT
-- 3) 新增数据由后端 SnowflakeIdGenerator 生成 ID
-- 注意：由于三张主表被外键引用，需先临时删除外键，再改列，再恢复外键。

SET NAMES utf8mb4;

-- 0) 删除相关外键（存在才执行）
SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_user_role DROP FOREIGN KEY fk_sys_user_role_user',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_role'
      AND CONSTRAINT_NAME = 'fk_sys_user_role_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_user_role DROP FOREIGN KEY fk_sys_user_role_role',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_role'
      AND CONSTRAINT_NAME = 'fk_sys_user_role_role'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_role_menu DROP FOREIGN KEY fk_sys_role_menu_role',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role_menu'
      AND CONSTRAINT_NAME = 'fk_sys_role_menu_role'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_role_menu DROP FOREIGN KEY fk_sys_role_menu_menu',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role_menu'
      AND CONSTRAINT_NAME = 'fk_sys_role_menu_menu'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_user_data_dept DROP FOREIGN KEY fk_sys_user_data_dept_user',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_data_dept'
      AND CONSTRAINT_NAME = 'fk_sys_user_data_dept_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1) sys_user.id 取消自增
SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_user MODIFY COLUMN id BIGINT NOT NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'id'
      AND EXTRA LIKE '%auto_increment%'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) sys_role.id 取消自增
SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_role MODIFY COLUMN id BIGINT NOT NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role'
      AND COLUMN_NAME = 'id'
      AND EXTRA LIKE '%auto_increment%'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) sys_menu.id 取消自增
SET @ddl := (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE sys_menu MODIFY COLUMN id BIGINT NOT NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_menu'
      AND COLUMN_NAME = 'id'
      AND EXTRA LIKE '%auto_increment%'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 恢复外键（不存在才新增）
SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user_role ADD CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_role'
      AND CONSTRAINT_NAME = 'fk_sys_user_role_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user_role ADD CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_role'
      AND CONSTRAINT_NAME = 'fk_sys_user_role_role'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_role_menu ADD CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role_menu'
      AND CONSTRAINT_NAME = 'fk_sys_role_menu_role'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_role_menu ADD CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role_menu'
      AND CONSTRAINT_NAME = 'fk_sys_role_menu_menu'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user_data_dept ADD CONSTRAINT fk_sys_user_data_dept_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_data_dept'
      AND CONSTRAINT_NAME = 'fk_sys_user_data_dept_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
