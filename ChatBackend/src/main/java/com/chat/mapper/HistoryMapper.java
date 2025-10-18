package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.HistoryEntity;
import com.chat.entity.RootEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryMapper extends BaseMapper<HistoryEntity> {
}
