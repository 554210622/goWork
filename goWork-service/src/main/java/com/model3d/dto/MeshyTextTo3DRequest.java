package com.model3d.dto;

import lombok.Data;

/**
 * Meshy AI 文本生成3D请求DTO
 * 根据官方文档: https://docs.meshy.ai/zh/api/text-to-3d
 */
@Data
public class MeshyTextTo3DRequest {
    
    /**
     * 生成提示词 (必需)
     */
    private String prompt;
    
    /**
     * 负面提示词 (可选)
     */
    private String negative_prompt;
    
    /**
     * 模式 (可选): "preview" 或 "refine"
     * preview: 快速预览模式，生成时间约2-3分钟
     * refine: 精细模式，生成时间约10-15分钟，质量更高
     */
    private String mode = "preview";
    
    /**
     * 艺术风格 (可选)
     * 可选值: "realistic", "cartoon", "low-poly", "sculpture", "pbr"
     */
    private String art_style;
    
    /**
     * 种子值 (可选)
     * 用于控制随机性，相同种子会产生相似结果
     */
    private Integer seed;
    
    /**
     * AI模型版本 (可选)
     * 默认使用最新版本
     */
    private String ai_model;
    
    /**
     * 拓扑结构 (可选)
     * 可选值: "quad", "triangle"
     */
    private String topology;
    
    /**
     * 目标多边形数量 (可选)
     * 控制生成模型的复杂度
     */
    private Integer target_polycount;
}