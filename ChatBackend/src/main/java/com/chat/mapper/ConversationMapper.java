package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.ConversationEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
}
