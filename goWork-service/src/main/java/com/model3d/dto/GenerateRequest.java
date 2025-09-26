package com.model3d.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 3D模型生成请求DTO
 */
@Data
public class GenerateRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 生成提示词
     */
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    /**
     * 输入类型：text 或 image
     */
    @NotBlank(message = "输入类型不能为空")
    private String inputType;

    /**
     * 输入数据（文本内容或图片URL）
     */
    private String inputData;

    /**
     * 生成风格（可选）
     * 可选值: "realistic", "cartoon", "low-poly", "sculpture", "pbr"
     */
    private String style;

    /**
     * 生成质量（可选）
     * preview: 快速预览模式，生成时间约2-3分钟
     * high: 高质量模式，生成时间约10-15分钟
     */
    private String quality;
    
    /**
     * 负面提示词（可选）
     */
    private String negativePrompt;
    
    /**
     * 种子值（可选）
     */
    private Integer seed;
    
    /**
     * 拓扑结构（可选）
     * 可选值: "quad", "triangle"
     */
    private String topology;
    
    /**
     * 目标多边形数量（可选）
     */
    private Integer targetPolycount;
    
    /**
     * 是否启用原始UV（图片生成3D时使用）
     */
    private Boolean enableOriginalUv;
}