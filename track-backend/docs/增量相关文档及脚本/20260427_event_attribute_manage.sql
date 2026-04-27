-- 2026-04-27 事件管理 + 属性管理改造
-- 目标：
-- 1) 埋点配置升级为事件管理（track-config -> event-manage）
-- 2) 新增属性管理（track_attribute）
-- 3) track_config 新增 requirement_id，并将 params 结构切换为属性引用
-- 4) 补齐菜单与角色权限

SET NAMES utf8mb4;

START TRANSACTION;

-- 1) track_config 新增 requirement_id
SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE track_config ADD COLUMN requirement_id VARCHAR(36) DEFAULT NULL COMMENT ''关联需求ID(track_requirement.requirement_id)'' AFTER event_type',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'track_config'
    AND COLUMN_NAME = 'requirement_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE track_config ADD INDEX idx_track_config_requirement_id (requirement_id)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'track_config'
    AND INDEX_NAME = 'idx_track_config_requirement_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 新建属性管理表
CREATE TABLE IF NOT EXISTS track_attribute (
  id BIGINT NOT NULL AUTO_INCREMENT,
  attribute_id VARCHAR(32) NOT NULL COMMENT '业务ID，格式：ATTR+yyyyMMdd+8位序号',
  attribute_name VARCHAR(100) NOT NULL COMMENT '属性名称',
  attribute_field VARCHAR(100) NOT NULL COMMENT '属性字段',
  attribute_type VARCHAR(20) NOT NULL COMMENT '属性类型：user/system/custom',
  source_type VARCHAR(50) DEFAULT NULL COMMENT '来源类型(custom时有效)',
  source_value VARCHAR(500) DEFAULT NULL COMMENT '变量路径或静态值',
  interface_id BIGINT DEFAULT NULL COMMENT '接口ID(api_data时有效)',
  interface_path VARCHAR(500) DEFAULT NULL COMMENT '接口路径',
  default_value VARCHAR(255) DEFAULT NULL COMMENT '取值失败默认值',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=生效,1=已删除',
  create_by VARCHAR(64) DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT NULL,
  create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
  update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_track_attribute_attribute_id (attribute_id),
  KEY idx_track_attribute_type_status (attribute_type, status),
  KEY idx_track_attribute_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) 扩展 dict_id_sequence 键长度，支持 ATTRyyyyMMdd
SET @ddl := (
  SELECT IF(
    CHARACTER_MAXIMUM_LENGTH < 32,
    'ALTER TABLE dict_id_sequence MODIFY COLUMN biz_date VARCHAR(32) NOT NULL COMMENT ''序列键，例如yyyyMMdd或ATTRyyyyMMdd''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dict_id_sequence'
    AND COLUMN_NAME = 'biz_date'
  LIMIT 1
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 菜单改造：track-config -> event-manage
UPDATE sys_menu
SET menu_name = '事件管理',
    menu_code = 'event-manage',
    path = '/event-manage',
    perms = NULL,
    update_time = NOW(3)
WHERE id = 3;

UPDATE sys_menu
SET menu_code = 'event-manage:view',
    perms = 'event-manage:view',
    update_time = NOW(3)
WHERE id = 4;

UPDATE sys_menu
SET menu_code = 'event-manage:add',
    perms = 'event-manage:add',
    update_time = NOW(3)
WHERE id = 5;

UPDATE sys_menu
SET menu_code = 'event-manage:edit',
    perms = 'event-manage:edit',
    update_time = NOW(3)
WHERE id = 6;

UPDATE sys_menu
SET menu_code = 'event-manage:delete',
    perms = 'event-manage:delete',
    update_time = NOW(3)
WHERE id = 7;

-- 5) 新增属性管理菜单与按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, path, icon, sort_order, menu_type, perms, status, create_time, update_time)
VALUES
  (32, 2, '属性管理', 'attribute-manage', '/attribute-manage', 'CollectionTag', 2, 2, NULL, 1, NOW(3), NOW(3)),
  (33, 32, '查看', 'attribute:view', NULL, NULL, 1, 3, 'attribute:view', 1, NOW(3), NOW(3)),
  (34, 32, '新增', 'attribute:add', NULL, NULL, 2, 3, 'attribute:add', 1, NOW(3), NOW(3)),
  (35, 32, '编辑', 'attribute:edit', NULL, NULL, 3, 3, 'attribute:edit', 1, NOW(3), NOW(3)),
  (36, 32, '删除', 'attribute:delete', NULL, NULL, 4, 3, 'attribute:delete', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  menu_name = VALUES(menu_name),
  menu_code = VALUES(menu_code),
  path = VALUES(path),
  icon = VALUES(icon),
  sort_order = VALUES(sort_order),
  menu_type = VALUES(menu_type),
  perms = VALUES(perms),
  status = VALUES(status),
  update_time = VALUES(update_time);

UPDATE sys_menu SET sort_order = 3, update_time = NOW(3) WHERE id = 8;
UPDATE sys_menu SET sort_order = 4, update_time = NOW(3) WHERE id = 13;
UPDATE sys_menu SET sort_order = 5, update_time = NOW(3) WHERE id = 27;

-- 6) 清理业务/开发角色旧权限并重灌
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
WHERE r.role_code IN ('business', 'developer');

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

COMMIT;
