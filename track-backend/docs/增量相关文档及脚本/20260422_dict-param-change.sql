-- 2026-04-22 参数维护功能增量脚本
-- 执行前请先备份数据库

SET NAMES utf8mb4;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS dict_param (
    id BIGINT NOT NULL AUTO_INCREMENT,
    param_id VARCHAR(32) NOT NULL,
    param_name VARCHAR(100) NOT NULL,
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
    biz_date VARCHAR(8) NOT NULL,
    seq INT NOT NULL,
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (biz_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, icon, sort_order, menu_type, perms, status, create_time, update_time)
VALUES (15, '参数维护', 'system-dict-param', '/system/dict-param', 'Operation', 3, 2, NULL, 1, NOW(3), NOW(3))
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

-- 参数维护不通过角色菜单配置赋权：
-- 1) admin 因全量菜单策略可见；
-- 2) developer 由后端菜单服务特判可见；
-- 3) 接口操作由后端角色硬校验（admin/developer）。

COMMIT;
