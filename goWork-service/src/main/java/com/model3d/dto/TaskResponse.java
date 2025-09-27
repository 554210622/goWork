package com.model3d.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务响应DTO
 * 适配Tripo AI官方API响应格式
 */
@Data
public class TaskResponse {

    /**
     * 本地任务ID
     */
    private Long id;

    /**
     * Tripo AI任务ID
     */
    private String taskId;

    /**
     * 任务类型
     * 可选值: text_to_model, image_to_model, multiview_to_model等
     */
    private String taskType;

    /**
     * 生成提示词
     */
    private String prompt;

    /**
     * 输入类型
     */
    private String inputType;

    /**
     * 输入数据
     */
    private String inputData;

    /**
     * 任务状态
     * finalized状态: SUCCESS(success), FAILED(failed/banned/expired)
     * ongoing状态: PENDING(queued), IN_PROGRESS(running)
     */
    private String status;

    /**
     * 生成的3D模型URL (GLB格式)
     */
    private String modelUrl;

    /**
     * 基础模型URL (无纹理)
     */
    private String baseModelUrl;

    /**
     * PBR材质模型URL
     */
    private String pbrModelUrl;

    /**
     * 预览图/渲染图像URL
     */
    private String previewUrl;

    /**
     * 纹理图像URL
     */
    private String textureImageUrl;

    /**
     * 法线贴图URL
     */
    private String normalMapUrl;

    /**
     * 金属度贴图URL
     */
    private String metallicMapUrl;

    /**
     * 粗糙度贴图URL
     */
    private String roughnessMapUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 进度信息
     */
    private ProgressInfo progress;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 使用的风格
     */
    private String style;

    /**
     * 面数统计
     */
    private Integer faceCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 关联的文件列表
     */
    private List<ModelFileDto> files;

    /**
     * 进度信息
     */
    @Data
    public static class ProgressInfo {
        /**
         * 进度百分比 (0-100)
         */
        private Integer percentage;

        /**
         * 当前阶段描述
         */
        private String stage;

        /**
         * 预计剩余时间（秒）
         */
        private Integer estimatedTimeRemaining;
    }

    /**
     * 模型文件信息
     */
    @Data
    public static class ModelFileDto {
        private Long id;
        
        /**
         * 文件类型
         * 可选值: model, texture, normal_map, metallic_map, roughness_map, preview等
         */
        private String fileType;
        
        /**
         * 文件格式
         * 可选值: glb, fbx, obj, jpg, png, webp等
         */
        private String fileFormat;
        
        private String fileUrl;
        private String fileName;
        private Long fileSize;
        
        /**
         * 文件描述
         */
        private String description;
        
        /**
         * 是否为主要文件
         */
        private Boolean isPrimary;
    }

    // ========== 便捷方法 ==========

    /**
     * 判断任务是否已完成（成功或失败）
     */
    public boolean isFinalized() {
        return "SUCCESS".equals(status) || "FAILED".equals(status);
    }

    /**
     * 判断任务是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 判断任务是否正在进行中
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status) || "PENDING".equals(status);
    }

    /**
     * 获取主要模型URL（优先返回PBR模型，其次是普通模型）
     */
    public String getPrimaryModelUrl() {
        if (pbrModelUrl != null && !pbrModelUrl.trim().isEmpty()) {
            return pbrModelUrl;
        }
        return modelUrl;
    }
}