package com.chat.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.entity.ConversationEntity;
import com.chat.service.ConversationService;
import com.chat.util.ConversationLockUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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


@Component
// 只有当配置task.enable.conversation为true时，才实例化该类
@ConditionalOnProperty(name = "task.enable.conversation", havingValue = "true")
@Slf4j
public class RealTimeQueryTask {
    // 常量配置：建议通过配置文件注入，增强灵活性
    private static final String REDIS_KEY = "conversation:RealTime";
    // 默认过期时间（天），可改为配置注入
    private static final long EXPIRATION_DAYS = 2;
    // 每次查询的最大数量，避免数据量过大
    private static final int MAX_LIMIT = 100;


    @Autowired
    private ConversationService conversationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 每天读取一次100个会话，并保存到redis中
     */
    @Scheduled(fixedRate = 1000*60*60*24,initialDelay = 60000)
    public void clearConversation() {
        try {
            // 1. 查询数据：限制数量，避免OOM
            List<ConversationEntity> conversationList = conversationService.query()
                    .eq("type", 1)
                    .last("ORDER BY RAND() LIMIT " + MAX_LIMIT) // 随机排序 + 限制数量
                    .list();

            if (conversationList.isEmpty()) {
                log.warn("未查询到type=1的群聊会话，不更新Redis缓存");
                return;
            }

            // 2. 缓存到Redis：使用序列化器确保对象正确存储
            redisTemplate.opsForValue().set(REDIS_KEY, conversationList);
            // 设置过期时间
            redisTemplate.expire(REDIS_KEY, EXPIRATION_DAYS, TimeUnit.DAYS);

            log.info("会话缓存任务执行成功，缓存{}条数据到Redis，key={}，过期时间{}天",
                    conversationList.size(), REDIS_KEY, EXPIRATION_DAYS);

        } catch (Exception e) {
            // 3. 异常处理：捕获所有异常，避免任务终止
            log.error("会话缓存任务执行失败", e);
            // 可选：发送告警通知（如邮件、企业微信）
        }
    }
}
