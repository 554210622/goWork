package com.model3d.dto;

import lombok.Data;

/**
 * Meshy AI 图片生成3D请求DTO
 * 根据官方文档: https://docs.meshy.ai/zh/api/image-to-3d
 */
@Data
public class MeshyImageTo3DRequest {
    
    /**
     * 图片URL (必需)
     * 支持的格式: JPEG, PNG, WEBP
     * 最大尺寸: 2048x2048
     */
    private String image_url;
    
    /**
     * 是否启用原始UV (可选)
     * 默认: false
     * 启用后会保持原始图片的UV映射
     */
    private Boolean enable_original_uv;
    
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