package com.chat.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.entity.ConversationEntity;
import com.chat.service.ConversationService;
import com.chat.util.ConversationLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 定时任务,清除conversation里面的用户少于2的私聊会话
 */
@Component
// 只有当配置task.enable.conversation为true时，才实例化该类
@ConditionalOnProperty(name = "task.enable.conversation", havingValue = "true")
@Slf4j
public class ConversationTask {
    @Autowired
    private ConversationService conversationService;

//    /**
//     * 每天凌晨2点清理用户数少于2的私聊会话
//     */
//    @Scheduled(cron = "0 0 2 * * ?")
    /**
     * 每 10 秒向所有客户端推送当前时间
     */
    @Scheduled(fixedRate = 1000*60*60)
    public void clearConversation() {
        LambdaQueryWrapper<ConversationEntity> query = new LambdaQueryWrapper<>();
        query.eq(ConversationEntity::getType, 0)
                .and(q -> q
                        .notLike(ConversationEntity::getConversation, ",") // 不含逗号（用户数≤1）
                        .or()
                        .isNull(ConversationEntity::getConversation) // 字段为null（用户数=0）
                        .or()
                        .eq(ConversationEntity::getConversation, "") // 字段为空字符串（用户数=0）
                );

        List<ConversationEntity> list = conversationService.list(query);
        if (list.isEmpty()) {
            log.info("无需要清理的私聊会话");
            return;
        }

        // 提取ID批量删除
        List<String> needDeleteIds = list.stream()
                .map(ConversationEntity::getId)
                .collect(Collectors.toList());

        boolean success = conversationService.removeByIds(needDeleteIds);
        if (success) {
            log.info("清理完成，共删除 {} 个用户数少于2的私聊会话", needDeleteIds.size());
            // 清理对应的锁对象
            needDeleteIds.forEach(ConversationLockUtil::removeMemberLock);
        } else {
            log.error("清理失败，需删除的会话ID：{}", needDeleteIds);
        }
    }
}