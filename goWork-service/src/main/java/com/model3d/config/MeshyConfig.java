package com.model3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Meshy AI配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "meshy.api")
public class MeshyConfig {
    
    /**
     * API密钥
     */
    private String key;
    
    /**
     * API基础URL
     */
    private String baseUrl;
    
    /**
     * 请求超时时间(毫秒)
     */
    private int timeout = 30000;
}