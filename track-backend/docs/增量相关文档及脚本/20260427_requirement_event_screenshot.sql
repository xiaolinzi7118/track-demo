SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS track_file_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_id VARCHAR(36) NOT NULL COMMENT '文件业务ID(uuid去横线)',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_ext VARCHAR(16) NOT NULL COMMENT '文件后缀(jpg/png)',
    content_type VARCHAR(64) NOT NULL COMMENT 'MIME类型',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_data LONGBLOB NOT NULL COMMENT '文件内容',
    create_by BIGINT DEFAULT NULL COMMENT '上传人ID',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_track_file_asset_file_id (file_id),
    KEY idx_track_file_asset_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE track_requirement
    ADD COLUMN screenshot_file_id VARCHAR(36) DEFAULT NULL COMMENT '需求截图文件ID(track_file_asset.file_id)' AFTER description,
    ADD KEY idx_track_requirement_screenshot (screenshot_file_id);

ALTER TABLE track_config
    ADD COLUMN page_screenshot_file_id VARCHAR(36) DEFAULT NULL COMMENT '页面截图文件ID(track_file_asset.file_id)' AFTER requirement_id,
    ADD KEY idx_track_config_page_screenshot (page_screenshot_file_id);
