package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<FileEntity> {
}
