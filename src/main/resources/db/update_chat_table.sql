-- 为chat表添加is_read字段
ALTER TABLE `chat` ADD COLUMN `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）';