package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.vo.ConversationVo;
import com.chat.entity.ConversationEntity;

import java.util.List;

public interface ConversationService extends IService<ConversationEntity> {
    Page<ConversationVo> getConversationList(Integer pageNum, Integer pageSize, String key);

    List<ConversationVo> getConversationListSelf(String userIdStr);
}
