package com.model3d.controller;

import com.model3d.common.Result;
import com.model3d.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "图片和文件上传相关接口")
public class UploadController {

    @Autowired
    private OssService ossService;

    /**
     * 上传图片到OSS
     */
    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传图片到阿里云OSS，返回公网访问URL")
    public Result<UploadResponse> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @Parameter(description = "业务类型（可选）") @RequestParam(required = false) String businessType) {
        
        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 2. 验证文件类型
            String contentType = file.getContentType();
            if (!isImageFile(contentType)) {
                return Result.error("只支持图片文件 (jpg, jpeg, png, gif, webp)");
            }
            
            // 3. 验证文件大小 (最大10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }
            
            log.info("开始上传图片: fileName={}, size={}, contentType={}, userId={}", 
                    file.getOriginalFilename(), file.getSize(), contentType, userId);
            
            // 4. 上传到OSS
            String ossUrl = ossService.uploadFile(file, "upload/images", businessType);
            
            // 5. 构建响应
            UploadResponse response = new UploadResponse();
            response.setUrl(ossUrl);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());
            response.setContentType(contentType);
            response.setUploadTime(LocalDateTime.now());
            response.setUserId(userId);
            response.setBusinessType(businessType);
            
            log.info("图片上传成功: fileName={}, ossUrl={}", file.getOriginalFilename(), ossUrl);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("图片上传失败: fileName={}", file.getOriginalFilename(), e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/images")
    @Operation(summary = "批量上传图片", description = "批量上传多张图片到阿里云OSS")
    public Result<BatchUploadResponse> uploadImages(
            @Parameter(description = "图片文件数组") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @Parameter(description = "业务类型（可选）") @RequestParam(required = false) String businessType) {
        
        try {
            if (files == null || files.length == 0) {
                return Result.error("请选择要上传的文件");
            }
            
            if (files.length > 10) {
                return Result.error("单次最多上传10个文件");
            }
            
            BatchUploadResponse batchResponse = new BatchUploadResponse();
            
            for (MultipartFile file : files) {
                try {
                    if (!file.isEmpty() && isImageFile(file.getContentType())) {
                        String ossUrl = ossService.uploadFile(file, "upload/images", businessType);
                        
                        UploadResponse response = new UploadResponse();
                        response.setUrl(ossUrl);
                        response.setFileName(file.getOriginalFilename());
                        response.setFileSize(file.getSize());
                        response.setContentType(file.getContentType());
                        response.setUploadTime(LocalDateTime.now());
                        response.setUserId(userId);
                        response.setBusinessType(businessType);
                        
                        batchResponse.getSuccessFiles().add(response);
                    } else {
                        batchResponse.getFailedFiles().add(file.getOriginalFilename() + ": 不支持的文件类型");
                    }
                } catch (Exception e) {
                    batchResponse.getFailedFiles().add(file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
            
            batchResponse.setTotalCount(files.length);
            batchResponse.setSuccessCount(batchResponse.getSuccessFiles().size());
            batchResponse.setFailedCount(batchResponse.getFailedFiles().size());
            
            return Result.success(batchResponse);
            
        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }

    /**
     * 通过URL上传图片
     */
    @PostMapping("/image-from-url")
    @Operation(summary = "从URL上传图片", description = "从指定URL下载图片并上传到OSS")
    public Result<UploadResponse> uploadImageFromUrl(
            @RequestBody UrlUploadRequest request) {
        
        try {
            log.info("开始从URL上传图片: url={}, userId={}", request.getUrl(), request.getUserId());
            
            // 上传到OSS
            String ossUrl = ossService.uploadFromUrl(request.getUrl(), null, "upload/images");
            
            // 构建响应
            UploadResponse response = new UploadResponse();
            response.setUrl(ossUrl);
            response.setFileName(extractFileNameFromUrl(request.getUrl()));
            response.setUploadTime(LocalDateTime.now());
            response.setUserId(request.getUserId());
            response.setBusinessType(request.getBusinessType());
            response.setSourceUrl(request.getUrl());
            
            log.info("从URL上传图片成功: sourceUrl={}, ossUrl={}", request.getUrl(), ossUrl);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("从URL上传图片失败: url={}", request.getUrl(), e);
            return Result.error("从URL上传失败: " + e.getMessage());
        }
    }

    /**
     * 验证是否为图片文件
     */
    private boolean isImageFile(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") && 
               (contentType.contains("jpeg") || contentType.contains("jpg") || 
                contentType.contains("png") || contentType.contains("gif") || 
                contentType.contains("webp") || contentType.contains("bmp"));
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < url.length() - 1) {
                String fileName = url.substring(lastSlash + 1);
                int questionMark = fileName.indexOf('?');
                if (questionMark >= 0) {
                    fileName = fileName.substring(0, questionMark);
                }
                return fileName;
            }
        } catch (Exception e) {
            log.debug("无法从URL提取文件名: {}", url);
        }
        return "image_" + System.currentTimeMillis();
    }

    /**
     * 上传响应DTO
     */
    @Data
    public static class UploadResponse {
        private String url;              // OSS公网访问URL
        private String fileName;         // 原始文件名
        private Long fileSize;           // 文件大小（字节）
        private String contentType;      // 文件类型
        private LocalDateTime uploadTime; // 上传时间
        private Long userId;             // 用户ID
        private String businessType;     // 业务类型
        private String sourceUrl;        // 源URL（从URL上传时）
    }

    /**
     * 批量上传响应DTO
     */
    @Data
    public static class BatchUploadResponse {
        private Integer totalCount = 0;
        private Integer successCount = 0;
        private Integer failedCount = 0;
        private java.util.List<UploadResponse> successFiles = new java.util.ArrayList<>();
        private java.util.List<String> failedFiles = new java.util.ArrayList<>();
    }

    /**
     * URL上传请求DTO
     */
    @Data
    public static class UrlUploadRequest {
        private String url;              // 图片URL
        private Long userId;             // 用户ID（可选）
        private String businessType;     // 业务类型（可选）
    }
}