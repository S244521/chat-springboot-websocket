package com.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.entity.ConversationEntity;
import com.chat.mapper.ConversationMapper;
import com.chat.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, ConversationEntity> implements ConversationService {
}
