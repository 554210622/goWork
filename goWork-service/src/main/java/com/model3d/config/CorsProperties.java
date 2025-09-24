package com.model3d.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @Author WR
 * @Version 1.0
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private boolean allowCredentials;

    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }

    public String getAllowedMethods() { return allowedMethods; }
    public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }

    public String getAllowedHeaders() { return allowedHeaders; }
    public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }

    public boolean isAllowCredentials() { return allowCredentials; }
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
}
