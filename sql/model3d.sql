/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : model3d

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 28/09/2025 22:00:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for model_files
-- ----------------------------
DROP TABLE IF EXISTS `model_files`;
CREATE TABLE `model_files`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '关联的任务ID',
  `file_type` enum('obj','fbx','glb','mtl','png','jpg') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件URL',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE,
  CONSTRAINT `model_files_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `model_tasks` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '3D模型文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of model_files
-- ----------------------------

-- ----------------------------
-- Table structure for model_tasks
-- ----------------------------
DROP TABLE IF EXISTS `model_tasks`;
CREATE TABLE `model_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `task_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Meshy AI任务ID',
  `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '生成提示词',
  `input_type` enum('text','image') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '输入类型',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输入数据',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `model_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `base_model_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '基础模型URL（无纹理）',
  `pbr_model_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'PBR材质模型URL',
  `texture_image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '纹理图像URL',
  `normal_map_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '法线贴图URL',
  `metallic_map_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '金属度贴图URL',
  `roughness_map_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '粗糙度贴图URL',
  `preview_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标志',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '任务类型：text-文本生成, image-图片生成',
  `generation_stage` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'single' COMMENT '生成阶段：preview-预览阶段, refine-精细化阶段, single-单阶段',
  `preview_task_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览任务的Meshy ID（用于精细化阶段）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_model_tasks_user_id_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_model_tasks_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_model_tasks_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_model_tasks_generation_stage`(`generation_stage` ASC) USING BTREE,
  INDEX `idx_model_tasks_preview_task_id`(`preview_task_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '3D模型生成任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of model_tasks
-- ----------------------------
INSERT INTO `model_tasks` VALUES (19, 1, 'bd66b699-097d-464e-a470-b0523feace7a', '一只五爪金龙，盘旋', 'text', '一只五爪金龙，盘旋', 'SUCCESS', NULL, NULL, 'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/27/1758988553814_2a4215ef.glb', NULL, NULL, NULL, NULL, 'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/27/1758988552422_a78006e2.webp', NULL, '2025-09-27 23:54:00', '2025-09-27 23:55:35', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (21, 1, '19b00dda-0a9c-4c53-b3fb-12fc0709544b', '一匹马', 'text', '一匹马', 'SUCCESS', NULL, NULL, 'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1758991575389_b72ce0f6.glb', NULL, NULL, NULL, NULL, 'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1758991574725_327d6e21.webp', NULL, '2025-09-28 00:44:07', '2025-09-28 00:46:13', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (22, 1, '7bc93c1c-b8e7-4eb8-91da-84651c31decb', '一头牛', 'text', '一头牛', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759063382812_ff925857.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759063381658_8442c722.webp', NULL, '2025-09-28 20:41:34', '2025-09-28 20:43:00', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (24, 1, '88f42f80-6e1b-4175-a6a4-6ab11a76fc7e', '生成一只猴子，金色的毛发', 'text', '生成一只猴子，金色的毛发', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759064347368_0492b50e.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759064346221_095c9cd3.webp', NULL, '2025-09-28 20:57:50', '2025-09-28 20:59:05', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (25, 1, '48fb5261-b394-4fbc-94f5-2e4c1d8b3c52', '生成一只老鼠，带蝴蝶结元素', 'text', '生成一只老鼠，带蝴蝶结元素', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759064725151_348b071c.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759064723459_e28cb83d.webp', NULL, '2025-09-28 21:04:03', '2025-09-28 21:05:21', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (26, 1, '4faefe00-ae5d-43ff-bda2-bc38b9a76a1c', NULL, 'image', 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/upload/images/2025/09/28/5ab30418-ce52-437d-9d18-75a00c0dc77f.jpg', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759065007896_7cd4388e.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759065005654_a24820f4.webp', NULL, '2025-09-28 21:07:52', '2025-09-28 21:09:27', NULL, 0, 'image', 'single', NULL);
INSERT INTO `model_tasks` VALUES (27, 1, 'debf24c2-6199-45b3-b2a4-477a85092319', '一只大公鸡', 'text', '一只大公鸡', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759065347544_3fed6412.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759065346827_26927032.webp', NULL, '2025-09-28 21:14:41', '2025-09-28 21:15:46', NULL, 0, 'text', 'single', NULL);
INSERT INTO `model_tasks` VALUES (28, 1, '2c86b3a0-f424-4055-9356-c6e38ce23bcd', NULL, 'image', 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/upload/images/2025/09/28/e1052855-f0c7-4af9-87f0-a118fc7ea0fb.jpg', 'SUCCESS', NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/28/1759065579561_2611009b.glb', NULL, NULL, NULL, NULL, 'https://seven-cow.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/28/1759065578951_93cffcce.webp', NULL, '2025-09-28 21:17:03', '2025-09-28 21:19:38', NULL, 0, 'image', 'single', NULL);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'testuser', 'test@example.com', NULL, '2025-09-24 22:57:37', '2025-09-24 22:57:37', 0);
INSERT INTO `users` VALUES (2, 'admin', 'admin@example.com', NULL, '2025-09-24 22:57:37', '2025-09-24 22:57:37', 0);

SET FOREIGN_KEY_CHECKS = 1;
