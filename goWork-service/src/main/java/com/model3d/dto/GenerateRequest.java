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
     */
    private String style;

    /**
     * 生成质量（可选）
     */
    private String quality;
}