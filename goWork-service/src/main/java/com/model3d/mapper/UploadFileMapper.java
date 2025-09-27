package com.model3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model3d.entity.UploadFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传文件Mapper
 */
@Mapper
public interface UploadFileMapper extends BaseMapper<UploadFile> {

    /**
     * 根据MD5查找文件（用于去重）
     */
    UploadFile selectByMd5(@Param("fileMd5") String fileMd5, @Param("userId") Long userId);

    /**
     * 更新引用次数
     */
    int updateReferenceCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 查找过期文件
     */
    List<UploadFile> selectExpiredFiles(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查找用户的文件
     */
    List<UploadFile> selectByUserId(@Param("userId") Long userId, @Param("businessType") String businessType);

    /**
     * 统计用户文件大小
     */
    Long sumFileSizeByUserId(@Param("userId") Long userId);
}