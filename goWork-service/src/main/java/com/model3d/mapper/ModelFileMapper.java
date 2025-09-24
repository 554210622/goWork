package com.model3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model3d.entity.ModelFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型文件Mapper接口
 */
@Mapper
public interface ModelFileMapper extends BaseMapper<ModelFile> {
}