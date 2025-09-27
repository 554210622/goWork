package com.model3d.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.model3d.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS文件存储服务
 */
@Slf4j
@Service
public class OssService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /**
     * 直接上传文件到OSS
     * 
     * @param file 文件对象
     * @param pathPrefix 路径前缀
     * @param businessType 业务类型
     * @return OSS文件URL
     */
    public String uploadFile(org.springframework.web.multipart.MultipartFile file, String pathPrefix, String businessType) {
        try {
            log.info("开始上传文件到OSS: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

            // 1. 生成OSS文件路径
            String ossFilePath = generateOssFilePathForUpload(file.getOriginalFilename(), pathPrefix, businessType);
            
            // 2. 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            metadata.setCacheControl("max-age=31536000"); // 1年缓存
            
            // 3. 上传到OSS
            try (java.io.InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(), 
                    ossFilePath, 
                    inputStream, 
                    metadata
                );
                
                ossClient.putObject(putRequest);
                
                // 4. 生成访问URL
                String ossUrl = generateAccessUrl(ossFilePath);
                
                log.info("文件上传成功: fileName={} -> ossUrl={}", file.getOriginalFilename(), ossUrl);
                
                return ossUrl;
            }

        } catch (Exception e) {
            log.error("上传文件到OSS失败: fileName={}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 从URL下载文件并上传到OSS
     * 
     * @param sourceUrl 源文件URL
     * @param fileName 文件名（可选，为空时自动生成）
     * @param fileType 文件类型（model/texture/preview等）
     * @return OSS文件URL
     */
    public String uploadFromUrl(String sourceUrl, String fileName, String fileType) {
        try {
            log.info("开始从URL下载并上传到OSS: sourceUrl={}, fileType={}", sourceUrl, fileType);

            // 1. 从URL下载文件
            URL url = new URL(sourceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setRequestMethod("GET");
            
            // 设置User-Agent避免被拒绝
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("下载文件失败，HTTP状态码: " + responseCode);
            }

            // 2. 获取文件信息
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();
            
            log.debug("文件信息: contentType={}, contentLength={}", contentType, contentLength);

            // 3. 生成OSS文件路径
            String ossFilePath = generateOssFilePath(fileName, sourceUrl, fileType);
            
            // 4. 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            if (contentLength > 0) {
                metadata.setContentLength(contentLength);
            }
            
            // 设置缓存控制
            metadata.setCacheControl("max-age=31536000"); // 1年缓存
            
            // 5. 上传到OSS
            try (InputStream inputStream = connection.getInputStream()) {
                PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(), 
                    ossFilePath, 
                    inputStream, 
                    metadata
                );
                
                ossClient.putObject(putRequest);
                
                // 6. 生成访问URL
                String ossUrl = generateAccessUrl(ossFilePath);
                
                log.info("文件上传成功: sourceUrl={} -> ossUrl={}", sourceUrl, ossUrl);
                
                return ossUrl;
            }

        } catch (Exception e) {
            log.error("从URL上传文件到OSS失败: sourceUrl={}", sourceUrl, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }



    /**
     * 生成OSS文件路径（用于直接上传）
     */
    private String generateOssFilePathForUpload(String originalFileName, String pathPrefix, String businessType) {
        // 生成唯一文件名
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
        
        // 按日期组织文件路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        String fullPath = ossConfig.getPathPrefix() + pathPrefix + "/" + datePath;
        if (businessType != null && !businessType.trim().isEmpty()) {
            fullPath += "/" + businessType;
        }
        fullPath += "/" + uniqueFileName;
        
        return fullPath;
    }

    /**
     * 生成OSS文件路径（用于URL下载）
     */
    private String generateOssFilePath(String fileName, String sourceUrl, String fileType) {
        // 如果没有提供文件名，从URL中提取或生成
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = extractFileNameFromUrl(sourceUrl);
            if (fileName == null) {
                // 生成唯一文件名
                String extension = getFileExtensionFromUrl(sourceUrl);
                fileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
            }
        }
        
        // 按日期和类型组织文件路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return ossConfig.getPathPrefix() + fileType + "/" + datePath + "/" + fileName;
    }

    /**
     * 获取文件扩展名（从文件名）
     */
    private String getFileExtension(String fileName) {
        if (fileName != null) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < fileName.length() - 1) {
                return fileName.substring(lastDot + 1);
            }
        }
        return "";
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                return path.substring(lastSlash + 1);
            }
        } catch (Exception e) {
            log.debug("无法从URL提取文件名: {}", url);
        }
        return null;
    }

    /**
     * 从URL获取文件扩展名
     */
    private String getFileExtensionFromUrl(String url) {
        try {
            String fileName = extractFileNameFromUrl(url);
            if (fileName != null) {
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot >= 0 && lastDot < fileName.length() - 1) {
                    return fileName.substring(lastDot + 1);
                }
            }
        } catch (Exception e) {
            log.debug("无法从URL获取文件扩展名: {}", url);
        }
        return "";
    }

    /**
     * 生成访问URL
     */
    private String generateAccessUrl(String ossFilePath) {
        if (ossConfig.getCustomDomain() != null && !ossConfig.getCustomDomain().trim().isEmpty()) {
            // 使用自定义域名
            return "https://" + ossConfig.getCustomDomain() + "/" + ossFilePath;
        } else {
            // 使用默认OSS域名
            return "https://" + ossConfig.getBucketName() + "." + 
                   ossConfig.getEndpoint().replace("https://", "").replace("http://", "") + 
                   "/" + ossFilePath;
        }
    }

    @Autowired
    private com.model3d.mapper.ModelTaskMapper modelTaskMapper;

    /**
     * 更新任务URL
     */
    public void updateTaskUrl(Long taskId, String fieldName, String ossUrl) {
        try {
            com.model3d.entity.ModelTask task = modelTaskMapper.selectById(taskId);
            if (task != null) {
                switch (fieldName) {
                    case "model_url":
                        task.setModelUrl(ossUrl);
                        break;
                    case "base_model_url":
                        task.setBaseModelUrl(ossUrl);
                        break;
                    case "pbr_model_url":
                        task.setPbrModelUrl(ossUrl);
                        break;
                    case "preview_url":
                        task.setPreviewUrl(ossUrl);
                        break;
                    case "texture_image_url":
                        task.setTextureImageUrl(ossUrl);
                        break;
                    case "normal_map_url":
                        task.setNormalMapUrl(ossUrl);
                        break;
                    case "metallic_map_url":
                        task.setMetallicMapUrl(ossUrl);
                        break;
                    case "roughness_map_url":
                        task.setRoughnessMapUrl(ossUrl);
                        break;
                }
                modelTaskMapper.updateById(task);
                log.info("任务URL更新成功: taskId={}, field={}, ossUrl={}", taskId, fieldName, ossUrl);
            }
        } catch (Exception e) {
            log.error("更新任务URL失败: taskId={}, field={}", taskId, fieldName, e);
        }
    }

    /**
     * 删除OSS文件
     */
    public void deleteFile(String ossFilePath) {
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), ossFilePath);
            log.info("OSS文件删除成功: {}", ossFilePath);
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", ossFilePath, e);
        }
    }
}