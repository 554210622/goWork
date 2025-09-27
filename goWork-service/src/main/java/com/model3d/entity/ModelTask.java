package com.model3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 3D模型生成任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_tasks")
public class ModelTask {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Meshy AI任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 生成提示词
     */
    @TableField("prompt")
    private String prompt;

    /**
     * 输入类型：text-文本, image-图片
     */
    @TableField("input_type")
    private String inputType;

    /**
     * 输入数据（文本内容或图片URL）
     */
    @TableField("input_data")
    private String inputData;
    
    /**
     * 任务类型：用于API查询 (text/image)
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 生成阶段：preview-预览阶段, refine-精细化阶段
     */
    @TableField("generation_stage")
    private String generationStage;

    /**
     * 预览任务的Meshy ID（用于精细化阶段）
     */
    @TableField("preview_task_id")
    private String previewTaskId;

    /**
     * 任务状态：PENDING-等待中, IN_PROGRESS-处理中, SUCCEEDED-已完成, FAILED-失败
     */
    @TableField("status")
    private String status;

    /**
     * 生成的3D模型URL（主模型）
     */
    @TableField("model_url")
    private String modelUrl;

    /**
     * 基础模型URL（无纹理）
     */
    @TableField("base_model_url")
    private String baseModelUrl;

    /**
     * PBR材质模型URL
     */
    @TableField("pbr_model_url")
    private String pbrModelUrl;

    /**
     * 纹理图像URL
     */
    @TableField("texture_image_url")
    private String textureImageUrl;

    /**
     * 法线贴图URL
     */
    @TableField("normal_map_url")
    private String normalMapUrl;

    /**
     * 金属度贴图URL
     */
    @TableField("metallic_map_url")
    private String metallicMapUrl;

    /**
     * 粗糙度贴图URL
     */
    @TableField("roughness_map_url")
    private String roughnessMapUrl;

    /**
     * 预览图URL
     */
    @TableField("preview_url")
    private String previewUrl;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}