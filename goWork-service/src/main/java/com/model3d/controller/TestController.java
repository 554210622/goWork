package com.model3d.controller;

import com.aliyun.oss.OSS;
import com.model3d.common.Result;
import com.model3d.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证各种配置
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /**
     * 测试OSS配置
     */
    @GetMapping("/oss")
    public Result<Map<String, Object>> testOss() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 检查配置信息
            result.put("bucketName", ossConfig.getBucketName());
            result.put("endpoint", ossConfig.getEndpoint());
            result.put("pathPrefix", ossConfig.getPathPrefix());
            
            // 2. 测试存储桶是否存在
            boolean bucketExists = ossClient.doesBucketExist(ossConfig.getBucketName());
            result.put("bucketExists", bucketExists);
            
            if (bucketExists) {
                // 3. 获取存储桶信息
                try {
                    var bucketInfo = ossClient.getBucketInfo(ossConfig.getBucketName());
                    result.put("bucketLocation", bucketInfo.getBucket().getLocation());
                    result.put("bucketCreationDate", bucketInfo.getBucket().getCreationDate());
                    result.put("bucketStorageClass", bucketInfo.getBucket().getStorageClass());
                } catch (Exception e) {
                    result.put("bucketInfoError", e.getMessage());
                }
                
                result.put("status", "success");
                result.put("message", "OSS配置正确，存储桶存在且可访问");
                
                return Result.success(result);
            } else {
                result.put("status", "error");
                result.put("message", "存储桶不存在：" + ossConfig.getBucketName());
                result.put("solution", "请在阿里云OSS控制台创建存储桶，或修改配置文件中的bucket-name");
                
                return new Result<>(404, "存储桶不存在：" + ossConfig.getBucketName(), result);
            }
            
        } catch (Exception e) {
            log.error("OSS连接测试失败", e);
            
            result.put("status", "error");
            result.put("message", "OSS连接失败：" + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            
            // 提供解决建议
            if (e.getMessage().contains("InvalidAccessKeyId")) {
                result.put("solution", "AccessKey ID无效，请检查配置");
            } else if (e.getMessage().contains("SignatureDoesNotMatch")) {
                result.put("solution", "AccessKey Secret错误，请检查配置");
            } else if (e.getMessage().contains("NoSuchBucket")) {
                result.put("solution", "存储桶不存在，请创建存储桶或修改配置");
            } else {
                result.put("solution", "请检查网络连接和OSS配置");
            }
            
            return new Result<>(500, "OSS测试失败：" + e.getMessage(), result);
        }
    }

    /**
     * 获取OSS配置信息（脱敏）
     */
    @GetMapping("/oss-config")
    public Result<Map<String, Object>> getOssConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("bucketName", ossConfig.getBucketName());
        config.put("endpoint", ossConfig.getEndpoint());
        config.put("pathPrefix", ossConfig.getPathPrefix());
        config.put("customDomain", ossConfig.getCustomDomain());
        
        // AccessKey脱敏显示
        String accessKeyId = ossConfig.getAccessKeyId();
        if (accessKeyId != null && accessKeyId.length() > 8) {
            config.put("accessKeyId", accessKeyId.substring(0, 4) + "****" + accessKeyId.substring(accessKeyId.length() - 4));
        } else {
            config.put("accessKeyId", "未配置");
        }
        
        return Result.success(config);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "gowork-service");
        return Result.success(health);
    }
}