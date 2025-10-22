package com.chat.components;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/ws/{sid}") // sid 代表 conversation_id 或 chat_id
@Slf4j
public class WebSocketServer {

    /**
     * 存储所有聊天室的会话：
     * - Key: String sid (聊天室ID)
     * - Value: Set<Session> (该聊天室中所有客户端的会*话集合)
     */
    private static final ConcurrentHashMap<String, Set<Session>> chatRoomSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立成功时调用
     * @param session WebSocket 会话
     * @param sid     聊天室标识 (从路径参数中获取)
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        // 1. 获取或创建该聊天室的会话集合
        // 使用 computeIfAbsent 保证线程安全地创建 Set
        Set<Session> sessions = chatRoomSessions.computeIfAbsent(sid, k -> new CopyOnWriteArraySet<>());

        // 2. 将当前会话加入集合
        sessions.add(session);

        log.info("客户端 [{}] 加入聊天室 [{}]，当前聊天室在线数：{}", session.getId(), sid, sessions.size());
        log.info("所有聊天室总连接数：{}", getTotalConnections());
    }

    /**
     * 连接关闭时调用
     * @param session 会话
     * @param sid 客户端标识
     */
    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid) {
        // 1. 获取该聊天室的会话集合
        Set<Session> sessions = chatRoomSessions.get(sid);
        if (sessions != null) {
            // 2. 从集合中移除当前会话
            sessions.remove(session);
            log.info("客户端 [{}] 退出聊天室 [{}]，当前聊天室剩余在线数：{}", session.getId(), sid, sessions.size());

            // 3. (可选) 如果聊天室为空，可以从Map中移除
            if (sessions.isEmpty()) {
                chatRoomSessions.remove(sid);
                log.info("聊天室 [{}] 已空，已移除。", sid);
            }
        }
        log.info("所有聊天室总连接数：{}", getTotalConnections());
    }

    /**
     * 收到客户端消息时调用
     * @param message 客户端发送的消息
     * @param sid     客户端所在的聊天室标识
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到聊天室 [{}] 的消息：{}", sid, message);

        // 将消息广播给同一聊天室的所有人
        sendMessageToChatRoom(sid, message);
    }

    /**
     * 连接出错时调用
     * @param session 会话
     * @param error   异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接出错，会话ID: {}", session.getId(), error);
    }

    /**
     * 给指定的聊天室广播消息
     * @param sid     聊天室标识
     * @param message 消息内容
     */
    public void sendMessageToChatRoom(String sid, String message) {
        Set<Session> sessions = chatRoomSessions.get(sid);
        if (sessions != null && !sessions.isEmpty()) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        log.error("给客户端 [{}] 发送消息失败（聊天室 [{}]）：", session.getId(), sid, e);
                    }
                }
            }
        }
    }

    /**
     * 获取当前所有连接的总数
     * @return a long
     */
    public long getTotalConnections() {
        return chatRoomSessions.values().stream().mapToLong(Set::size).sum();
    }
}