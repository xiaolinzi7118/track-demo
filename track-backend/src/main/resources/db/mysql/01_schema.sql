SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    primary_dept_id BIGINT DEFAULT NULL COMMENT '所属部门(dict_param_item.id)',
    status TINYINT NOT NULL DEFAULT 1,
    is_builtin_super_admin TINYINT NOT NULL DEFAULT 0 COMMENT '内置超级管理员标识(0-否,1-是)',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_primary_dept_id (primary_dept_id),
    KEY idx_sys_user_builtin_super_admin (is_builtin_super_admin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    path VARCHAR(255) DEFAULT NULL,
    icon VARCHAR(100) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    menu_type TINYINT NOT NULL COMMENT '1-directory, 2-page, 3-button',
    perms VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (menu_code),
    KEY idx_sys_menu_parent_sort (parent_id, sort_order),
    KEY idx_sys_menu_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_sys_role_menu_menu_id (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS track_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_name VARCHAR(255) NOT NULL,
    event_code VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    requirement_id VARCHAR(36) DEFAULT NULL COMMENT '关联需求ID(track_requirement.requirement_id)',
    description TEXT,
    params TEXT,
    url_pattern TEXT,
    dept_id BIGINT DEFAULT NULL COMMENT '数据所属部门(dict_param_item.id)',
    status TINYINT DEFAULT 1,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_track_config_event_code (event_code),
    KEY idx_track_config_event_type (event_type),
    KEY idx_track_config_requirement_id (requirement_id),
    KEY idx_track_config_create_time (create_time),
    KEY idx_track_config_dept_create (dept_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS track_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_code VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    url VARCHAR(1024) DEFAULT NULL,
    params TEXT,
    user_id VARCHAR(128) DEFAULT NULL,
    session_id VARCHAR(128) DEFAULT NULL,
    user_agent VARCHAR(1024) DEFAULT NULL,
    ip VARCHAR(64) DEFAULT NULL,
    duration BIGINT DEFAULT NULL,
    dept_id BIGINT DEFAULT NULL COMMENT '数据所属部门(dict_param_item.id)',
    event_time DATETIME(3) DEFAULT NULL,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_track_data_event_time (event_time),
    KEY idx_track_data_query (event_code, event_type, user_id),
    KEY idx_track_data_dept_event_time (dept_id, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS api_interface (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    description TEXT,
    dept_id BIGINT DEFAULT NULL COMMENT '数据所属部门(dict_param_item.id)',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_api_interface_path (path),
    KEY idx_api_interface_dept_create (dept_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dict_param (
    id BIGINT NOT NULL AUTO_INCREMENT,
    param_id VARCHAR(32) NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '0-normal, 1-system',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-active, 1-deleted',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_by VARCHAR(64) DEFAULT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    active_name VARCHAR(100) GENERATED ALWAYS AS (CASE WHEN status = 0 THEN param_name ELSE NULL END) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_param_id (param_id),
    UNIQUE KEY uk_dict_param_active_name (active_name),
    KEY idx_dict_param_status_create (status, create_time),
    KEY idx_dict_param_name (param_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dict_param_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    param_id VARCHAR(32) NOT NULL,
    item_code VARCHAR(100) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-active, 1-deleted',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_by VARCHAR(64) DEFAULT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_dict_param_item_param_status (param_id, status),
    KEY idx_dict_param_item_param (param_id),
    CONSTRAINT fk_dict_param_item_param_id FOREIGN KEY (param_id) REFERENCES dict_param (param_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dict_id_sequence (
    biz_date VARCHAR(32) NOT NULL COMMENT '序列键，例如yyyyMMdd或ATTRyyyyMMdd',
    seq INT NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (biz_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
