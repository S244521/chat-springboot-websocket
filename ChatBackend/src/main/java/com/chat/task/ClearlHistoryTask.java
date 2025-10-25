package com.chat.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.entity.ConversationEntity;
import com.chat.entity.HistoryEntity;
import com.chat.service.ConversationService;
import com.chat.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
// 只有当配置task.enable.conversation为true时，才实例化该类
@ConditionalOnProperty(name = "task.enable.conversation", havingValue = "true")
@Slf4j
public class ClearlHistoryTask {

    @Autowired
    private HistoryService historyService;

    /**
     * 清除群聊会话超过3天的历史聊天记录 （1小时执行一次）记录最多保留3天一个小时的记录
     */
    @Scheduled(fixedRate = 1000*60*60*1,initialDelay = 60000)
    public void clearHistory() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // 构建删除条件：创建时间 < 3天前（即 现在 - 创建时间 > 3天）
        LambdaQueryWrapper<HistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(HistoryEntity::getCreatetime, threeDaysAgo); // lt 表示 "小于"
        historyService.remove(queryWrapper);
        log.info("已删除 {} 天前的历史记录", 3);
    }
}
