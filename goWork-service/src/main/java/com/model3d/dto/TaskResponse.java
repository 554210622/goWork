package com.model3d.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务响应DTO
 */
@Data
public class TaskResponse {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * Meshy AI任务ID
     */
    private String taskId;

    /**
     * 生成提示词
     */
    private String prompt;

    /**
     * 输入类型
     */
    private String inputType;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 生成的3D模型URL
     */
    private String modelUrl;

    /**
     * 预览图URL
     */
    private String previewUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 关联的文件列表
     */
    private List<ModelFileDto> files;

    @Data
    public static class ModelFileDto {
        private Long id;
        private String fileType;
        private String fileUrl;
        private String fileName;
        private Long fileSize;
    }
}