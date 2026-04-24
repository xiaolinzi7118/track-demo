-- 2026-04-23 RBAC + 部门数据权限改造增量脚本
-- 兼容 MySQL 5.7/8.0（不使用 ALTER TABLE ... ADD COLUMN IF NOT EXISTS）
-- 目标：1个所属部门 + 多个数据授权部门

SET NAMES utf8mb4;

-- 1) dict_param.is_system
SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE dict_param ADD COLUMN is_system TINYINT NOT NULL DEFAULT 0 COMMENT ''0-normal, 1-system''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dict_param'
      AND COLUMN_NAME = 'is_system'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 初始化部门参数与默认部门
INSERT INTO dict_param (param_id, param_name, is_system, status, create_by, create_time, update_by, update_time)
VALUES ('SYS_DEPT', '部门', 1, 0, 'system', NOW(3), 'system', NOW(3))
ON DUPLICATE KEY UPDATE
    param_name = VALUES(param_name),
    is_system = VALUES(is_system),
    status = 0,
    update_by = VALUES(update_by),
    update_time = VALUES(update_time);

INSERT INTO dict_param_item (param_id, item_code, item_name, status, create_by, create_time, update_by, update_time)
SELECT 'SYS_DEPT', 'DEFAULT', '默认部门', 0, 'system', NOW(3), 'system', NOW(3)
WHERE NOT EXISTS (
    SELECT 1
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
);

-- 3) sys_user.primary_dept_id
SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD COLUMN primary_dept_id BIGINT DEFAULT NULL COMMENT ''所属部门(dict_param_item.id)'' AFTER avatar',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'primary_dept_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_user u
JOIN (
    SELECT id
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
    ORDER BY id ASC
    LIMIT 1
) d
SET u.primary_dept_id = COALESCE(u.primary_dept_id, d.id);

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD INDEX idx_sys_user_primary_dept_id (primary_dept_id)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'idx_sys_user_primary_dept_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 业务表 dept_id 字段与索引
SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE track_config ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT ''数据所属部门(dict_param_item.id)''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'track_config'
      AND COLUMN_NAME = 'dept_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE track_config ADD INDEX idx_track_config_dept_create (dept_id, create_time)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'track_config'
      AND INDEX_NAME = 'idx_track_config_dept_create'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE api_interface ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT ''数据所属部门(dict_param_item.id)''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'api_interface'
      AND COLUMN_NAME = 'dept_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE api_interface ADD INDEX idx_api_interface_dept_create (dept_id, create_time)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'api_interface'
      AND INDEX_NAME = 'idx_api_interface_dept_create'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE track_data ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT ''数据所属部门(dict_param_item.id)''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'track_data'
      AND COLUMN_NAME = 'dept_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE track_data ADD INDEX idx_track_data_dept_event_time (dept_id, event_time)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'track_data'
      AND INDEX_NAME = 'idx_track_data_dept_event_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 回填业务表 dept_id
UPDATE track_config tc
JOIN (
    SELECT id
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
    ORDER BY id ASC
    LIMIT 1
) d
SET tc.dept_id = COALESCE(tc.dept_id, d.id);

UPDATE api_interface ai
JOIN (
    SELECT id
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
    ORDER BY id ASC
    LIMIT 1
) d
SET ai.dept_id = COALESCE(ai.dept_id, d.id);

UPDATE track_data td
JOIN (
    SELECT id
    FROM dict_param_item
    WHERE param_id = 'SYS_DEPT' AND item_code = 'DEFAULT' AND status = 0
    ORDER BY id ASC
    LIMIT 1
) d
SET td.dept_id = COALESCE(td.dept_id, d.id);

-- 6) 用户数据授权关系表
CREATE TABLE IF NOT EXISTS sys_user_data_dept (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL COMMENT '数据授权部门(dict_param_item.id)',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_data_dept (user_id, dept_id),
    KEY idx_sys_user_data_dept_dept_id (dept_id),
    CONSTRAINT fk_sys_user_data_dept_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_data_dept_dept FOREIGN KEY (dept_id) REFERENCES dict_param_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_user_data_dept (user_id, dept_id, create_by, create_time)
SELECT u.id, u.primary_dept_id, 'system', NOW(3)
FROM sys_user u
WHERE u.primary_dept_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    create_by = VALUES(create_by);
