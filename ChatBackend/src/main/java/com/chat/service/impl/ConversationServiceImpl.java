package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.vo.ConversationVo;
import com.chat.vo.UserVo;
import com.chat.entity.ConversationEntity;
import com.chat.entity.UserEntity;
import com.chat.mapper.ConversationMapper;
import com.chat.service.ConversationService;
import com.chat.service.UserService;
import com.chat.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, ConversationEntity> implements ConversationService {

    @Autowired
    private UserService userService;

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
            conversationVo.setType(conversationEntity.getType());
            Set<Integer> split = StringUtil.split(conversationEntity.getConversation());

            List<UserVo> userList = userService.getUserList(split);
            conversationVo.setUsers(userList);

            return conversationVo;
        }).toList();

        conversationVoPage.setRecords(conversationVos);
        return conversationVoPage;
    }

    @Override
    public List<ConversationVo> getConversationListSelf(String userIdStr) {
        // 查询当前用户参与的所有会话
        // 核心：筛选conversation字段（格式如"1,2,3"）中包含当前用户ID的记录
        List<ConversationEntity> conversationList = this.lambdaQuery()
                // 模糊查询：确保用户ID作为独立成员存在（避免匹配到"123"包含"23"的情况）
            .and(q -> q
                    .like(ConversationEntity::getConversation, "," + userIdStr + ",") // 中间位置（如"1,2,3"中的"2"）
                    .or()
                    .likeRight(ConversationEntity::getConversation, userIdStr + ",") // 开头位置（如"2,3"中的"2"）
                    .or()
                    .likeLeft(ConversationEntity::getConversation, "," + userIdStr) // 结尾位置（如"1,2"中的"2"）
                    .or()
                    .eq(ConversationEntity::getConversation, userIdStr) // 唯一成员（如"2"）
            )
            .list(); // 执行查询

        List<ConversationVo> conversationVos = conversationList.stream().map(conversationEntity -> {
            ConversationVo conversationVo = new ConversationVo();
            conversationVo.setId(conversationEntity.getId());
            conversationVo.setName(conversationEntity.getName());
            conversationVo.setType(conversationEntity.getType());

            if(conversationEntity.getType() == 0){
                Set<Integer> split = StringUtil.split(conversationEntity.getConversation());
                split.remove(Integer.parseInt(userIdStr));
                if (split.size() == 1){
                    UserEntity user = userService.getById(split.iterator().next());
                    conversationVo.setName(user.getName());
                    conversationVo.setUser(new UserVo(user.getId(), user.getUsername(), user.getName(), user.getSex()));
                }else{
                    return null;
                }
            }else if(conversationEntity.getType() == 1){
                Set<Integer> split = StringUtil.split(conversationEntity.getConversation());
                List<UserVo> userList = userService.getUserList(split);
                conversationVo.setUsers(userList);
            }
            return conversationVo;
        })
        .filter(Objects::nonNull) // 关键：过滤掉所有null元素
        .toList();
        return conversationVos;
    }
}
