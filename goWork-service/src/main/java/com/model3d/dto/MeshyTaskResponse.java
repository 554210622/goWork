package com.model3d.dto;

import lombok.Data;
import java.util.List;

/**
 * Meshy AI 任务响应DTO
 */
@Data
public class MeshyTaskResponse {
    
    /**
     * 任务ID
     */
    private String id;
    
    /**
     * 任务状态：PENDING, IN_PROGRESS, SUCCEEDED, FAILED
     */
    private String status;
    
    /**
     * 创建时间
     */
    private String created_at;
    
    /**
     * 开始时间
     */
    private String started_at;
    
    /**
     * 完成时间
     */
    private String finished_at;
    
    /**
     * 任务结果
     */
    private TaskResult result;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 任务类型
     */
    private String task_type;
    
    /**
     * 输入参数
     */
    private Object input;
    
    @Data
    public static class TaskResult {
        /**
         * 模型URL
         */
        private String model_url;
        
        /**
         * 预览图URL
         */
        private String thumbnail_url;
        
        /**
         * 视频URL（如果有）
         */
        private String video_url;
        
        /**
         * 纹理URL列表
         */
        private List<String> texture_urls;
    }
}