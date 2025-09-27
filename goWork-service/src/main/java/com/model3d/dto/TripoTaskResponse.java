package com.model3d.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Tripo AI 任务响应
 * 根据官方文档: https://platform.tripo3d.ai/docs/generation
 * 完全按照tripo.md文档规范
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripoTaskResponse {
    
    /**
     * 响应代码，0表示成功，非零表示错误
     */
    private Integer code;
    
    /**
     * 响应数据 (仅在成功时出现)
     */
    private TripoTaskData data;
    
    /**
     * 错误消息 (仅在错误时出现)
     */
    private String message;
    
    /**
     * 建议 (仅在错误时出现)
     */
    private String suggestion;
    
    /**
     * 任务数据
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoTaskData {
        /**
         * 任务ID
         */
        @JsonProperty("task_id")
        private String taskId;
        
        /**
         * 任务类型
         * 可选值: text_to_model, image_to_model, multiview_to_model等
         */
        private String type;
        
        /**
         * 任务状态
         * finalized状态: success, failed, banned, expired
         * ongoing状态: queued, running
         */
        private String status;
        
        /**
         * 输入信息
         */
        private TripoInput input;
        
        /**
         * 输出结果 (仅在success状态时出现)
         */
        private TripoOutput output;
        
        /**
         * 进度百分比 (0-100)
         * 仅在ongoing状态时出现
         */
        private Integer progress;
        
        /**
         * 创建时间戳 (Unix时间戳)
         */
        @JsonProperty("create_time")
        private Long createTime;
        
        /**
         * 提示词 (仅text_to_model任务)
         */
        private String prompt;
        
        /**
         * 模型版本
         */
        @JsonProperty("model_version")
        private String modelVersion;
        
        /**
         * 错误信息 (仅在failed状态时出现)
         */
        @JsonProperty("error_message")
        private String errorMessage;
    }
    
    /**
     * 输入信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoInput {
        /**
         * 提示词 (text_to_model)
         */
        private String prompt;
        
        /**
         * 负面提示词 (text_to_model)
         */
        @JsonProperty("negative_prompt")
        private String negativePrompt;
        
        /**
         * 模型版本
         */
        @JsonProperty("model_version")
        private String modelVersion;
        
        /**
         * 图像种子
         */
        @JsonProperty("image_seed")
        private Integer imageSeed;
        
        /**
         * 模型种子
         */
        @JsonProperty("model_seed")
        private Integer modelSeed;
        
        /**
         * 纹理种子
         */
        @JsonProperty("texture_seed")
        private Integer textureSeed;
        
        /**
         * 风格
         */
        private String style;
        
        /**
         * 面数限制
         */
        @JsonProperty("face_limit")
        private Integer faceLimit;
        
        /**
         * 是否启用纹理
         */
        private Boolean texture;
        
        /**
         * 是否启用PBR
         */
        private Boolean pbr;
        
        /**
         * 纹理质量
         */
        @JsonProperty("texture_quality")
        private String textureQuality;
        
        /**
         * 纹理对齐 (image_to_model)
         */
        @JsonProperty("texture_alignment")
        private String textureAlignment;
        
        /**
         * 自动尺寸
         */
        @JsonProperty("auto_size")
        private Boolean autoSize;
        
        /**
         * 方向 (image_to_model)
         */
        private String orientation;
        
        /**
         * 四边形网格
         */
        private Boolean quad;
        
        /**
         * 压缩
         */
        private String compress;
        
        /**
         * 智能低面数
         */
        @JsonProperty("smart_low_poly")
        private Boolean smartLowPoly;
        
        /**
         * 生成部件
         */
        @JsonProperty("generate_parts")
        private Boolean generateParts;
        
        /**
         * 几何质量
         */
        @JsonProperty("geometry_quality")
        private String geometryQuality;
        
        /**
         * 文件信息 (image_to_model)
         */
        private TripoFileInfo file;
    }
    
    /**
     * 文件信息 (image_to_model)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoFileInfo {
        /**
         * 文件类型
         */
        private String type;
        
        /**
         * 文件令牌
         */
        @JsonProperty("file_token")
        private String fileToken;
        
        /**
         * 图像URL
         */
        private String url;
        
        /**
         * 对象信息
         */
        private TripoObjectInfo object;
    }
    
    /**
     * 对象信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoObjectInfo {
        /**
         * 存储桶
         */
        private String bucket;
        
        /**
         * 资源键
         */
        private String key;
    }
    
    /**
     * 输出结果 - 严格按照tripo.md文档
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripoOutput {
        /**
         * 主模型文件URL (GLB格式)
         * 包含几何体和纹理的完整模型
         */
        private String model;
        
        /**
         * 基础模型URL (GLB格式，无纹理)
         * 仅包含几何体，无纹理信息
         */
        @JsonProperty("base_model")
        private String baseModel;
        
        /**
         * PBR材质模型URL (GLB格式)
         * 包含PBR材质的高质量模型
         */
        @JsonProperty("pbr_model")
        private String pbrModel;
        
        /**
         * 渲染图像URL
         * 模型的预览渲染图
         */
        @JsonProperty("rendered_image")
        private String renderedImage;
        
        /**
         * 纹理图像URL
         * 模型的纹理贴图
         */
        @JsonProperty("texture_image")
        private String textureImage;
        
        /**
         * 法线贴图URL
         * PBR材质的法线贴图
         */
        @JsonProperty("normal_map")
        private String normalMap;
        
        /**
         * 金属度贴图URL
         * PBR材质的金属度贴图
         */
        @JsonProperty("metallic_map")
        private String metallicMap;
        
        /**
         * 粗糙度贴图URL
         * PBR材质的粗糙度贴图
         */
        @JsonProperty("roughness_map")
        private String roughnessMap;
        
        /**
         * 面数统计
         * 生成模型的实际面数
         */
        @JsonProperty("face_count")
        private Integer faceCount;
        
        /**
         * 顶点数统计
         * 生成模型的实际顶点数
         */
        @JsonProperty("vertex_count")
        private Integer vertexCount;
    }
}