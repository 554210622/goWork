package com.model3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tripo AI API配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "tripo.api")
public class TripoConfig {
    
    /**
     * API密钥
     */
    private String key;
    
    /**
     * API基础URL
     */
    private String baseUrl;
    
    /**
     * 请求超时时间（毫秒）
     */
    private Long timeout;
}