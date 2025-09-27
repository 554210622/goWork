package com.model3d.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传文件记录实体（可选）
 * 用于记录上传的文件信息，便于管理和统计
 */
@Data
@TableName("upload_files")
public class UploadFile {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * OSS文件URL
     */
    private String fileUrl;

    /**
     * OSS文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 业务类型
     * 如：avatar（头像）、product（商品图）、model_input（模型输入图）等
     */
    private String businessType;

    /**
     * 文件状态
     * ACTIVE：正常使用
     * DELETED：已删除
     * EXPIRED：已过期
     */
    private String status;

    /**
     * 源URL（从URL上传时记录）
     */
    private String sourceUrl;

    /**
     * 文件MD5值（用于去重）
     */
    private String fileMd5;

    /**
     * 引用次数（被使用的次数）
     */
    private Integer referenceCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 过期时间（可选，用于临时文件）
     */
    private LocalDateTime expireAt;

    /**
     * 备注信息
     */
    private String remark;
}