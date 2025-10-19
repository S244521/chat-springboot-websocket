package com.chat.components;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * @ServerEndpoint 注解标识该类为 WebSocket 端点，`/ws/{sid}` 是连接路径（sid 为客户端标识）
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    /** 存储所有在线会话：key 为客户端 sid，value 为 WebSocket 会话 */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功时调用
     * @param session WebSocket 会话
     * @param sid     客户端标识（从路径参数中获取）
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("客户端 [{}] 建立连接，当前在线数：{}", sid, SESSION_MAP.size());
    }

    /**
     * 连接关闭时调用
     * @param sid 客户端标识
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("客户端 [{}] 断开连接，当前在线数：{}", sid, SESSION_MAP.size());
    }

    /**
     * 收到客户端消息时调用
     * @param message 客户端发送的消息
     * @param sid     客户端标识
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到客户端 [{}] 的消息：{}", sid, message);
        // 可在这里处理消息（如广播、定向转发等）
        sendMessageToAll("服务端收到消息：" + message);
    }

    /**
     * 连接出错时调用
     * @param session 会话
     * @param error   异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接出错：", error);
    }

    /**
     * 给指定客户端发送消息
     * @param sid     客户端标识
     * @param message 消息内容
     */
    public static void sendMessage(String sid, String message) {
        Session session = SESSION_MAP.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("给客户端 [{}] 发送消息失败：", sid, e);
            }
        }
    }

    /**
     * 给所有在线客户端广播消息
     * @param message 消息内容
     */
    public static void sendMessageToAll(String message) {
        for (Session session : SESSION_MAP.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("广播消息失败：", e);
                }
            }
        }
    }
}