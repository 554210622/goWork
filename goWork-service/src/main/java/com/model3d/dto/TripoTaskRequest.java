package com.model3d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Tripo AI 任务请求基类
 * 根据官方文档: https://platform.tripo3d.ai/docs/generation
 */
@Data
public class TripoTaskRequest {
    
    /**
     * 任务类型 (必需)
     * 可选值: text_to_model, image_to_model, multiview_to_model等
     */
    private String type;
    
    /**
     * 模型版本 (可选)
     * 可选值: Turbo-v1.0-20250506, v3.0-20250812, v2.5-20250123, v2.0-20240919, v1.4-20240625
     * 默认: v2.5-20250123
     */
    @JsonProperty("model_version")
    private String modelVersion;
    
    /**
     * 面数限制 (可选)
     * 限制输出模型的面数，如果smart_low_poly=true，应为1000~16000
     * 如果quad=true，应为500~8000
     */
    @JsonProperty("face_limit")
    private Integer faceLimit;
    
    /**
     * 是否启用纹理 (可选)
     * 默认值: true
     */
    private Boolean texture;
    
    /**
     * 是否启用PBR (可选)
     * 默认值: true，如果设置为true，texture将被忽略并使用true
     */
    private Boolean pbr;
    
    /**
     * 纹理质量 (可选)
     * 可选值: standard(默认), detailed
     */
    @JsonProperty("texture_quality")
    private String textureQuality;
    
    /**
     * 自动尺寸 (可选)
     * 自动缩放模型到真实世界尺寸，单位为米
     * 默认值: false
     */
    @JsonProperty("auto_size")
    private Boolean autoSize;
    
    /**
     * 四边形网格 (可选)
     * 设置为true启用四边形网格输出
     * 注意: 启用此选项将强制输出为FBX模型
     */
    private Boolean quad;
    
    /**
     * 压缩 (可选)
     * 指定应用于纹理的压缩类型
     * 可选值: geometry
     */
    private String compress;
    
    /**
     * 智能低面数 (可选)
     * 生成具有手工制作拓扑的低面数网格
     * 默认值: false
     */
    @JsonProperty("smart_low_poly")
    private Boolean smartLowPoly;
    
    /**
     * 生成部件 (可选)
     * 生成分段3D模型，使每个部件可编辑
     * 默认值: false
     * 注意: 与texture=true或pbr=true不兼容，与quad=true不兼容
     */
    @JsonProperty("generate_parts")
    private Boolean generateParts;
    
    /**
     * 几何质量 (可选)
     * 仅对model_version>=v3.0-20250812有效
     * 可选值: standard(默认), detailed
     */
    @JsonProperty("geometry_quality")
    private String geometryQuality;
    
    /**
     * 风格 (可选)
     * 定义应用于3D模型的艺术风格或变换
     */
    private String style;
}