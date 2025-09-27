package com.model3d.controller;

import com.model3d.common.Result;
import com.model3d.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件下载和管理相关接口")
public class FileController {

    @Autowired
    private OssService ossService;

    /**
     * 获取文件下载链接
     */
    @GetMapping("/download-url")
    @Operation(summary = "获取文件下载链接", description = "获取OSS文件的临时下载链接")
    public Result<String> getDownloadUrl(
            @Parameter(description = "文件URL") @RequestParam String fileUrl,
            @Parameter(description = "链接有效期（秒），默认3600秒") @RequestParam(defaultValue = "3600") Integer expireSeconds) {
        
        try {
            // 如果是OSS URL，生成临时下载链接
            if (fileUrl.contains("aliyuncs.com") || fileUrl.contains("oss-")) {
                // 这里可以实现OSS临时链接生成逻辑
                // 暂时直接返回原URL
                return Result.success(fileUrl);
            } else {
                // 非OSS URL直接返回
                return Result.success(fileUrl);
            }
            
        } catch (Exception e) {
            log.error("获取下载链接失败: fileUrl={}", fileUrl, e);
            return Result.error("获取下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 文件信息接口
     */
    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "获取文件的基本信息")
    public Result<FileInfo> getFileInfo(@Parameter(description = "文件URL") @RequestParam String fileUrl) {
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUrl(fileUrl);
            fileInfo.setFileName(extractFileName(fileUrl));
            fileInfo.setFileType(getFileType(fileUrl));
            fileInfo.setIsOssFile(fileUrl.contains("aliyuncs.com") || fileUrl.contains("oss-"));
            
            return Result.success(fileInfo);
            
        } catch (Exception e) {
            log.error("获取文件信息失败: fileUrl={}", fileUrl, e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 文件信息DTO
     */
    public static class FileInfo {
        private String url;
        private String fileName;
        private String fileType;
        private Boolean isOssFile;

        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public Boolean getIsOssFile() { return isOssFile; }
        public void setIsOssFile(Boolean isOssFile) { this.isOssFile = isOssFile; }
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileName(String url) {
        try {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < url.length() - 1) {
                String fileName = url.substring(lastSlash + 1);
                // 移除查询参数
                int questionMark = fileName.indexOf('?');
                if (questionMark >= 0) {
                    fileName = fileName.substring(0, questionMark);
                }
                return fileName;
            }
        } catch (Exception e) {
            log.debug("无法从URL提取文件名: {}", url);
        }
        return "unknown";
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String url) {
        String fileName = extractFileName(url);
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < fileName.length() - 1) {
            String extension = fileName.substring(lastDot + 1).toLowerCase();
            switch (extension) {
                case "glb":
                case "fbx":
                case "obj":
                    return "model";
                case "jpg":
                case "jpeg":
                case "png":
                case "webp":
                    return "image";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }
}