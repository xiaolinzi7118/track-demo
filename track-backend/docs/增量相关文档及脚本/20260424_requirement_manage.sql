-- 2026-04-24 需求管理模块增量脚本
-- 包含：track_requirement、track_log、需求管理菜单与角色权限

SET NAMES utf8mb4;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS track_requirement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requirement_id VARCHAR(36) NOT NULL COMMENT '需求业务ID(uuid v4)',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    status VARCHAR(32) NOT NULL COMMENT '需求状态',
    priority VARCHAR(8) NOT NULL COMMENT '优先级:P0/P1/P2',
    business_line_code VARCHAR(64) NOT NULL COMMENT '所属业务线编码',
    business_line_name VARCHAR(128) NOT NULL COMMENT '所属业务线名称',
    dev_team_code VARCHAR(64) NOT NULL COMMENT '负责开发团队编码',
    dev_team_name VARCHAR(128) NOT NULL COMMENT '负责开发团队名称',
    expected_online_date DATE NOT NULL COMMENT '期望上线日期',
    description TEXT DEFAULT NULL COMMENT '需求描述(纯文本)',
    proposer_id BIGINT NOT NULL COMMENT '提出人ID',
    proposer_name VARCHAR(64) NOT NULL COMMENT '提出人名称',
    department VARCHAR(128) NOT NULL COMMENT '所属部门名称',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_track_requirement_requirement_id (requirement_id),
    KEY idx_track_requirement_status (status),
    KEY idx_track_requirement_priority (priority),
    KEY idx_track_requirement_proposer (proposer_id),
    KEY idx_track_requirement_business_line (business_line_code),
    KEY idx_track_requirement_create_time (create_time),
    KEY idx_track_requirement_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS track_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    log_type VARCHAR(64) NOT NULL COMMENT '日志类型，如 requirement_manage',
    requirement_id VARCHAR(36) NOT NULL COMMENT '需求业务ID',
    action_type VARCHAR(32) NOT NULL COMMENT '操作类型:CREATE/STATUS_CHANGE/EDIT_RESUBMIT',
    from_status VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    to_status VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    opinion VARCHAR(500) DEFAULT NULL COMMENT '操作意见',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(64) NOT NULL COMMENT '操作人名称',
    operate_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_track_log_type_biz_time (log_type, requirement_id, operate_time),
    KEY idx_track_log_operator_time (operator_id, operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, path, icon, sort_order, menu_type, perms, status, create_time, update_time)
VALUES
    (27, 2, '需求管理', 'requirement-manage', '/requirement-manage', 'Tickets', 4, 2, NULL, 1, NOW(3), NOW(3)),
    (28, 27, '查看', 'requirement-manage:view', NULL, NULL, 1, 3, 'requirement-manage:view', 1, NOW(3), NOW(3)),
    (29, 27, '新增', 'requirement-manage:add', NULL, NULL, 2, 3, 'requirement-manage:add', 1, NOW(3), NOW(3)),
    (30, 27, '状态变更', 'requirement-manage:status', NULL, NULL, 3, 3, 'requirement-manage:status', 1, NOW(3), NOW(3)),
    (31, 27, '重新提交', 'requirement-manage:resubmit', NULL, NULL, 4, 3, 'requirement-manage:resubmit', 1, NOW(3), NOW(3))
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

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'business'
  AND m.menu_code IN ('requirement-manage', 'requirement-manage:view', 'requirement-manage:add', 'requirement-manage:resubmit')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'developer'
  AND m.menu_code IN ('requirement-manage', 'requirement-manage:view', 'requirement-manage:status')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

COMMIT;

