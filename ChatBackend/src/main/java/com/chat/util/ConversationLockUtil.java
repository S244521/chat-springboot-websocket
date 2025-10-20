package com.chat.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 会话操作的锁工具类（单例）
 */
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * 会话操作的锁工具类（优化版：增加锁过期清理机制）
 */
public class ConversationLockUtil {
    // 会话创建的全局锁（保持不变）
    private static final ReentrantLock CREATE_LOCK = new ReentrantLock();

    // 存储会话锁的容器：key=会话ID（即ConversationEntity的id），value=带时间戳的锁对象
    private static final ConcurrentHashMap<String, TimedLock> MEMBER_LOCKS = new ConcurrentHashMap<>();

    // 锁的过期时间：7天（可根据业务调整）
    private static final long LOCK_EXPIRE_MILLIS = TimeUnit.DAYS.toMillis(7);

    /**
     * 带最后使用时间的锁包装类
     */
    private static class TimedLock {
        private final ReentrantLock lock = new ReentrantLock();
        private long lastUsedTime; // 最后一次使用锁的时间

        public TimedLock() {
            this.lastUsedTime = System.currentTimeMillis();
        }

        // 获取锁对象
        public ReentrantLock getLock() {
            // 每次获取锁时，更新最后使用时间
            this.lastUsedTime = System.currentTimeMillis();
            return lock;
        }

        // 检查锁是否过期
        public boolean isExpired() {
            return System.currentTimeMillis() - lastUsedTime > LOCK_EXPIRE_MILLIS;
        }
    }

    // 获取创建会话的全局锁（保持不变）
    public static ReentrantLock getCreateLock() {
        return CREATE_LOCK;
    }

    // 获取指定会话的成员修改锁（优化：更新时间戳）
    public static ReentrantLock getMemberLock(String conversationId) {
        // 1. 计算并获取锁（若不存在则创建）
        TimedLock timedLock = MEMBER_LOCKS.computeIfAbsent(conversationId, k -> new TimedLock());
        // 2. 返回锁对象（内部已更新最后使用时间）
        return timedLock.getLock();
    }

    // 手动删除会话锁（当会话被删除时调用）
    public static void removeMemberLock(String conversationId) {
        MEMBER_LOCKS.remove(conversationId);
    }

    // 定期清理过期锁（建议用定时任务调用）
    public static void cleanExpiredLocks() {
        MEMBER_LOCKS.entrySet().removeIf(entry -> {
            TimedLock timedLock = entry.getValue();
            // 若锁已过期，且当前没有线程持有锁，则删除
            return timedLock.isExpired() && !timedLock.getLock().isHeldByCurrentThread();
        });
    }
}
