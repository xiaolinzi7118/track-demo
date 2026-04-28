-- 2026-04-27 需求管理“开发团队部门化”与用户部门规则改造脚本
-- 目标：
-- 1) dict_param_item 增加附加属性 extra_attr
-- 2) track_requirement 增加 dev_team_dept_id
-- 3) 软删除 SYS_DEPT 下默认部门 DEFAULT

START TRANSACTION;

-- 1) dict_param_item.extra_attr
SET @ddl := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'dict_param_item'
          AND COLUMN_NAME = 'extra_attr'
    ),
    'SELECT 1',
    'ALTER TABLE dict_param_item ADD COLUMN extra_attr VARCHAR(64) DEFAULT NULL COMMENT ''附加属性'' AFTER item_name'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) track_requirement.dev_team_dept_id
SET @ddl := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'track_requirement'
          AND COLUMN_NAME = 'dev_team_dept_id'
    ),
    'SELECT 1',
    'ALTER TABLE track_requirement ADD COLUMN dev_team_dept_id BIGINT DEFAULT NULL COMMENT ''负责开发团队部门ID(dict_param_item.id)'' AFTER dev_team_name'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'track_requirement'
          AND INDEX_NAME = 'idx_track_requirement_dev_team_dept'
    ),
    'SELECT 1',
    'ALTER TABLE track_requirement ADD INDEX idx_track_requirement_dev_team_dept (dev_team_dept_id)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 软删除默认部门参数项（不做兼容）
UPDATE dict_param_item
SET status = 1,
    update_by = 'system',
    update_time = NOW(3)
WHERE param_id = 'SYS_DEPT'
  AND item_code = 'DEFAULT'
  AND status = 0;

-- 4) 清理引用默认部门的用户所属部门与数据授权
UPDATE sys_user u
JOIN dict_param_item d ON u.primary_dept_id = d.id
SET u.primary_dept_id = NULL
WHERE d.param_id = 'SYS_DEPT'
  AND d.item_code = 'DEFAULT';

DELETE sud
FROM sys_user_data_dept sud
JOIN dict_param_item d ON sud.dept_id = d.id
WHERE d.param_id = 'SYS_DEPT'
  AND d.item_code = 'DEFAULT';

-- 5) 需求开发团队部门ID回填：按 dev_team_code 对应 SYS_DEPT.item_code 且附加属性=开发
UPDATE track_requirement r
JOIN dict_param_item d
  ON d.param_id = 'SYS_DEPT'
 AND d.item_code = r.dev_team_code
 AND d.status = 0
 AND d.extra_attr = '开发'
SET r.dev_team_dept_id = d.id
WHERE r.dev_team_dept_id IS NULL;

COMMIT;
