package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.Vo.ConversationVo;
import com.chat.Vo.UserVo;
import com.chat.entity.ConversationEntity;
import com.chat.entity.UserEntity;
import com.chat.mapper.ConversationMapper;
import com.chat.service.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, ConversationEntity> implements ConversationService {
    @Override
    public Page<ConversationVo> getConversationList(Integer pageNum, Integer pageSize, String key) {
        Page<ConversationEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ConversationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", key);
        queryWrapper.eq("type", 1);
        Page<ConversationEntity> conversationEntityPage = this.page(page,queryWrapper);

        Page<ConversationVo> conversationVoPage = new Page<>();
        conversationVoPage.setCurrent(conversationEntityPage.getCurrent());
        conversationVoPage.setSize(conversationEntityPage.getSize());
        conversationVoPage.setTotal(conversationEntityPage.getTotal());
        conversationVoPage.setPages(conversationEntityPage.getPages());

        List<ConversationVo> conversationVos = conversationEntityPage.getRecords().stream().map(conversationEntity -> {
            ConversationVo conversationVo = new ConversationVo();
            conversationVo.setId(conversationEntity.getId());
            conversationVo.setName(conversationEntity.getName());

            return conversationVo;
        }).toList();

        conversationVoPage.setRecords(conversationVos);
        return conversationVoPage;
    }
}
