-- 上传文件记录表（可选）
-- 用于记录和管理上传的文件信息

CREATE TABLE `upload_files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `original_file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_url` text NOT NULL COMMENT 'OSS文件URL',
  `file_path` varchar(500) NOT NULL COMMENT 'OSS文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型：avatar/product/model_input等',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '文件状态：ACTIVE/DELETED/EXPIRED',
  `source_url` text DEFAULT NULL COMMENT '源URL（从URL上传时记录）',
  `file_md5` varchar(32) DEFAULT NULL COMMENT '文件MD5值（用于去重）',
  `reference_count` int(11) DEFAULT 1 COMMENT '引用次数',
  `last_access_time` datetime DEFAULT NULL COMMENT '最后访问时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间（可选）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_status` (`status`),
  KEY `idx_file_md5` (`file_md5`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件记录表';

-- 为现有的model_3d_tasks表添加文件记录关联（可选）
ALTER TABLE `model_3d_tasks` 
ADD COLUMN `input_file_id` bigint(20) DEFAULT NULL COMMENT '输入文件记录ID' AFTER `input_image_url`,
ADD COLUMN `output_file_ids` text DEFAULT NULL COMMENT '输出文件记录ID列表（JSON格式）' AFTER `texture_image_url`;

-- 添加索引
ALTER TABLE `model_3d_tasks` 
ADD KEY `idx_input_file_id` (`input_file_id`);

-- 示例数据（可选）
INSERT INTO `upload_files` (`user_id`, `original_file_name`, `file_url`, `file_path`, `file_size`, `content_type`, `business_type`, `remark`) VALUES
(1, 'test_image.jpg', 'https://your-bucket.oss-cn-hangzhou.aliyuncs.com/uploads/images/2024/01/test_image.jpg', 'uploads/images/2024/01/test_image.jpg', 102400, 'image/jpeg', 'model_input', '测试图片上传');