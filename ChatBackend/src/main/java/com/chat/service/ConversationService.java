package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.Vo.ConversationVo;
import com.chat.entity.ConversationEntity;

public interface ConversationService extends IService<ConversationEntity> {
    Page<ConversationVo> getConversationList(Integer pageNum, Integer pageSize, String key);
}
