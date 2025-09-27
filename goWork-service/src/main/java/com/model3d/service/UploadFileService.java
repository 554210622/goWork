package com.model3d.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.model3d.entity.UploadFile;
import com.model3d.mapper.UploadFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传文件管理服务（可选）
 * 提供文件记录管理、统计、清理等功能
 */
@Slf4j
@Service
public class UploadFileService {

    @Autowired
    private UploadFileMapper uploadFileMapper;

    /**
     * 保存文件记录
     */
    public UploadFile saveFileRecord(Long userId, String originalFileName, String fileUrl, 
                                   String filePath, Long fileSize, String contentType, 
                                   String businessType, String sourceUrl) {
        try {
            UploadFile uploadFile = new UploadFile();
            uploadFile.setUserId(userId);
            uploadFile.setOriginalFileName(originalFileName);
            uploadFile.setFileUrl(fileUrl);
            uploadFile.setFilePath(filePath);
            uploadFile.setFileSize(fileSize);
            uploadFile.setContentType(contentType);
            uploadFile.setBusinessType(businessType);
            uploadFile.setSourceUrl(sourceUrl);
            uploadFile.setStatus("ACTIVE");
            uploadFile.setReferenceCount(1);
            uploadFile.setLastAccessTime(LocalDateTime.now());
            uploadFile.setCreatedAt(LocalDateTime.now());
            uploadFile.setUpdatedAt(LocalDateTime.now());

            uploadFileMapper.insert(uploadFile);
            
            log.info("文件记录保存成功: fileUrl={}, userId={}", fileUrl, userId);
            
            return uploadFile;
            
        } catch (Exception e) {
            log.error("保存文件记录失败: fileUrl={}, userId={}", fileUrl, userId, e);
            return null;
        }
    }

    /**
     * 根据MD5查找重复文件（去重功能）
     */
    public UploadFile findByMd5(String fileMd5, Long userId) {
        return uploadFileMapper.selectByMd5(fileMd5, userId);
    }

    /**
     * 增加文件引用次数
     */
    public void incrementReference(Long fileId) {
        try {
            uploadFileMapper.updateReferenceCount(fileId, 1);
            
            // 更新最后访问时间
            UploadFile uploadFile = uploadFileMapper.selectById(fileId);
            if (uploadFile != null) {
                uploadFile.setLastAccessTime(LocalDateTime.now());
                uploadFileMapper.updateById(uploadFile);
            }
            
        } catch (Exception e) {
            log.error("增加文件引用次数失败: fileId={}", fileId, e);
        }
    }

    /**
     * 减少文件引用次数
     */
    public void decrementReference(Long fileId) {
        try {
            uploadFileMapper.updateReferenceCount(fileId, -1);
        } catch (Exception e) {
            log.error("减少文件引用次数失败: fileId={}", fileId, e);
        }
    }

    /**
     * 获取用户文件列表
     */
    public List<UploadFile> getUserFiles(Long userId, String businessType) {
        return uploadFileMapper.selectByUserId(userId, businessType);
    }

    /**
     * 统计用户文件总大小
     */
    public Long getUserTotalFileSize(Long userId) {
        Long totalSize = uploadFileMapper.sumFileSizeByUserId(userId);
        return totalSize != null ? totalSize : 0L;
    }

    /**
     * 标记文件为删除状态
     */
    public void markAsDeleted(Long fileId) {
        try {
            UploadFile uploadFile = uploadFileMapper.selectById(fileId);
            if (uploadFile != null) {
                uploadFile.setStatus("DELETED");
                uploadFile.setUpdatedAt(LocalDateTime.now());
                uploadFileMapper.updateById(uploadFile);
                
                log.info("文件标记为删除: fileId={}, fileUrl={}", fileId, uploadFile.getFileUrl());
            }
        } catch (Exception e) {
            log.error("标记文件删除失败: fileId={}", fileId, e);
        }
    }

    /**
     * 清理过期文件
     */
    public void cleanExpiredFiles() {
        try {
            List<UploadFile> expiredFiles = uploadFileMapper.selectExpiredFiles(LocalDateTime.now());
            
            for (UploadFile file : expiredFiles) {
                // 标记为过期
                file.setStatus("EXPIRED");
                file.setUpdatedAt(LocalDateTime.now());
                uploadFileMapper.updateById(file);
                
                // 这里可以添加实际删除OSS文件的逻辑
                log.info("文件已过期: fileId={}, fileUrl={}", file.getId(), file.getFileUrl());
            }
            
            log.info("清理过期文件完成，共处理 {} 个文件", expiredFiles.size());
            
        } catch (Exception e) {
            log.error("清理过期文件失败", e);
        }
    }

    /**
     * 获取文件统计信息
     */
    public FileStatistics getFileStatistics(Long userId) {
        try {
            QueryWrapper<UploadFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("status", "ACTIVE");
            
            List<UploadFile> files = uploadFileMapper.selectList(queryWrapper);
            
            FileStatistics stats = new FileStatistics();
            stats.setTotalCount(files.size());
            stats.setTotalSize(files.stream().mapToLong(f -> f.getFileSize() != null ? f.getFileSize() : 0).sum());
            stats.setImageCount((int) files.stream().filter(f -> f.getContentType() != null && f.getContentType().startsWith("image/")).count());
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取文件统计失败: userId={}", userId, e);
            return new FileStatistics();
        }
    }

    /**
     * 文件统计信息
     */
    public static class FileStatistics {
        private Integer totalCount = 0;
        private Long totalSize = 0L;
        private Integer imageCount = 0;

        // Getters and Setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        
        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
        
        public Integer getImageCount() { return imageCount; }
        public void setImageCount(Integer imageCount) { this.imageCount = imageCount; }
    }
}