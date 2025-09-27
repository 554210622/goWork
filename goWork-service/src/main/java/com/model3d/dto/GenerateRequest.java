package com.model3d.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 3D模型生成请求DTO
 * 适配Tripo AI官方API参数
 */
@Data
public class GenerateRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 生成提示词 (text_to_model必需)
     * 最大长度1024字符，约100个单词
     */
    private String prompt;

    /**
     * 输入类型：text 或 image
     */
    @NotBlank(message = "输入类型不能为空")
    private String inputType;

    /**
     * 输入数据（图片URL、file_token或base64）
     * 仅在image_to_model时使用
     */
    private String inputData;

    /**
     * 负面提示词（可选）
     * 最大长度255字符
     */
    private String negativePrompt;

    /**
     * 模型版本（可选）
     * 可选值: Turbo-v1.0-20250506, v3.0-20250812, v2.5-20250123, v2.0-20240919, v1.4-20240625
     * 默认: v2.5-20250123
     */
    private String modelVersion;

    /**
     * 图像种子（可选）
     * 用于基于提示词的过程的随机种子
     */
    private Integer imageSeed;

    /**
     * 模型种子（可选）
     * 用于模型生成的随机种子，控制几何生成过程
     */
    private Integer modelSeed;

    /**
     * 纹理种子（可选）
     * 用于纹理生成的随机种子
     */
    private Integer textureSeed;

    /**
     * 种子值（可选，兼容旧版本）
     * 会同时设置modelSeed和imageSeed
     */
    private Integer seed;

    /**
     * 生成风格（可选）
     * 支持Tripo AI的风格类型，如: person:person2cartoon, object:clay, object:steampunk,animal:venom,object:barbie
     * object:christmas  gold  ancient_bronze
     */
    private String style;

    /**
     * 面数限制（可选）
     * 限制输出模型的面数，如果smart_low_poly=true，应为1000~16000
     * 如果quad=true，应为500~8000
     */
    private Integer faceLimit;

    /**
     * 目标多边形数量（可选，兼容旧版本）
     * 会映射到faceLimit
     */
    private Integer targetPolycount;

    /**
     * 是否启用纹理（可选）
     * 默认值: true
     */
    private Boolean texture;

    /**
     * 是否启用PBR（可选）
     * 默认值: true，如果设置为true，texture将被忽略并使用true
     */
    private Boolean pbr;

    /**
     * 纹理质量（可选）
     * 可选值: standard(默认), detailed
     */
    private String textureQuality;

    /**
     * 纹理对齐（可选，仅image_to_model）
     * 可选值: original_image(默认), geometry
     */
    private String textureAlignment;

    /**
     * 自动尺寸（可选）
     * 自动缩放模型到真实世界尺寸，单位为米
     * 默认值: false
     */
    private Boolean autoSize;

    /**
     * 方向（可选，仅image_to_model）
     * 设置为align_image自动旋转模型以对齐原始图像
     * 默认值: default
     */
    private String orientation;

    /**
     * 四边形网格（可选）
     * 设置为true启用四边形网格输出
     * 注意: 启用此选项将强制输出为FBX模型
     */
    private Boolean quad;

    /**
     * 压缩（可选）
     * 指定应用于纹理的压缩类型
     * 可选值: geometry
     */
    private String compress;

    /**
     * 智能低面数（可选）
     * 生成具有手工制作拓扑的低面数网格
     * 默认值: false
     */
    private Boolean smartLowPoly;

    /**
     * 生成部件（可选）
     * 生成分段3D模型，使每个部件可编辑
     * 默认值: false
     * 注意: 与texture=true或pbr=true不兼容，与quad=true不兼容
     */
    private Boolean generateParts;

    /**
     * 几何质量（可选）
     * 仅对model_version>=v3.0-20250812有效
     * 可选值: standard(默认), detailed
     */
    private String geometryQuality;

    // ========== 兼容性字段 ==========

    /**
     * 生成质量（可选，兼容旧版本）
     * 会映射到相应的Tripo参数
     */
    private String quality;

    /**
     * 拓扑结构（可选，兼容旧版本）
     * 会映射到quad参数
     */
    private String topology;

    /**
     * 是否启用原始UV（兼容旧版本）
     * 会映射到textureAlignment参数
     */
    private Boolean enableOriginalUv;
}