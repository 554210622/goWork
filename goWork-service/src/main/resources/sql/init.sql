-- 创建数据库
CREATE DATABASE IF NOT EXISTS model3d CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE model3d;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) UNIQUE COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3D生成任务表
CREATE TABLE IF NOT EXISTS model_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    task_id VARCHAR(100) COMMENT 'Meshy AI任务ID',
    prompt TEXT COMMENT '生成提示词',
    input_type ENUM('text', 'image') COMMENT '输入类型',
    input_data TEXT COMMENT '输入数据',
    status ENUM('pending', 'processing', 'completed', 'failed') DEFAULT 'pending' COMMENT '任务状态',
    model_url VARCHAR(500) COMMENT '生成的3D模型URL',
    preview_url VARCHAR(500) COMMENT '预览图URL',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='3D模型生成任务表';

-- 3D模型文件表
CREATE TABLE IF NOT EXISTS model_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT COMMENT '关联的任务ID',
    file_type ENUM('obj', 'fbx', 'glb', 'mtl', 'png', 'jpg') COMMENT '文件类型',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_name VARCHAR(255) COMMENT '文件名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX idx_task_id (task_id),
    INDEX idx_file_type (file_type),
    FOREIGN KEY (task_id) REFERENCES model_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='3D模型文件表';

-- 插入测试用户
INSERT INTO users (username, email) VALUES 
('testuser', 'test@example.com'),
('admin', 'admin@example.com')
ON DUPLICATE KEY UPDATE username=VALUES(username);