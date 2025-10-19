package com.chat.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * WebSocket 定时任务，用于演示后端主动推消息
 */
//@Component
//@EnableScheduling
@Slf4j
public class WebSocketTask {

    /**
     * 每 10 秒向所有客户端推送当前时间
     */
    @Scheduled(fixedRate = 10000)
    public void sendTimeToAll() {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//        WebSocketServer.sendMessageToAll("服务端定时推送：" + time);
        log.info("已推送定时消息：{}", time);
    }
}
