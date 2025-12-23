-- 创建数据库
CREATE DATABASE IF NOT EXISTS lostfound DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lostfound;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
    `phone` VARCHAR(20) NOT NULL COMMENT '电话',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL（OSS）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常，1禁用）',
    `create_time` DATETIME NOT NULL COMMENT '注册时间',
    `update_time` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '管理员账号（唯一）',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '电话',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常，1禁用）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 失物/招领信息表
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '信息ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT NOT NULL COMMENT '描述',
    `type` VARCHAR(10) NOT NULL COMMENT '类型（lost/claim）',
    `location` VARCHAR(100) NOT NULL COMMENT '地点',
    `item_time` DATETIME NOT NULL COMMENT '时间',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片（OSS）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待审核，1已通过，2已拒绝，3已解决）',
    `user_id` BIGINT NOT NULL COMMENT '发布人ID（外键 → user.id）',
    `create_time` DATETIME NOT NULL COMMENT '发布时间',
    `update_time` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物/招领信息表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `from_user` BIGINT NOT NULL COMMENT '发送方ID（外键 → user.id）',
    `to_user` BIGINT NOT NULL COMMENT '接收方ID（外键 → user.id）',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL COMMENT '发送时间',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
    PRIMARY KEY (`id`),
    KEY `idx_from_user` (`from_user`),
    KEY `idx_to_user` (`to_user`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 初始化管理员账号
INSERT INTO `admin` (`username`, `password`, `real_name`, `phone`, `status`, `create_time`)
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', '管理员', '13800138000', 0, NOW());
-- 密码为：admin

-- 添加删除的字段（修复版本）
ALTER TABLE `admin` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '电话' AFTER `real_name`;
ALTER TABLE `admin` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常，1禁用）' AFTER `phone`;
ALTER TABLE `admin` ADD COLUMN `create_time` DATETIME NULL COMMENT '创建时间' AFTER `status`;

-- 更新现有记录设置默认值
UPDATE `admin` SET `phone` = '' WHERE `phone` IS NULL;
UPDATE `admin` SET `create_time` = NOW() WHERE `create_time` IS NULL;

-- 修改列为 NOT NULL 约束
ALTER TABLE `admin` MODIFY COLUMN `phone` VARCHAR(20) NOT NULL COMMENT '电话';
ALTER TABLE `admin` MODIFY COLUMN `create_time` DATETIME NOT NULL COMMENT '创建时间';
