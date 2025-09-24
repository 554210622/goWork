package com.model3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 3D模型文件实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_files")
public class ModelFile {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 文件类型：obj, fbx, glb, mtl等
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}