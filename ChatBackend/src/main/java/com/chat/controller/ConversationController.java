package com.chat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.Vo.ConversationVo;
import com.chat.common.Result;
import com.chat.entity.ConversationEntity;
import com.chat.service.ConversationService;
import com.chat.util.ConversationLockUtil;
import com.chat.util.JwtUtils;
import com.chat.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 建立会话
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    public Result<?> createConversation(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody ConversationEntity conversationEntity
    ) {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(7);

        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }
        // 查询群聊名称是否重复
        if(conversationEntity.getName()!=null&&conversationEntity.getType()==1){
            if(conversationService.query().eq("name",conversationEntity.getName()).exists()){
                return Result.error("群聊名称已存在");
            }
        }

        Long userId = JwtUtils.parseToken(token).get("userId", Long.class);
        Set<Integer> split = StringUtil.split(conversationEntity.getConversation());
        split.add(Math.toIntExact(userId));
        String standardization = StringUtil.standardization(split);


        String uid;
        // 最大10次超出显示当前不方便创建
        int retryCount = 0;
        ReentrantLock createLock = ConversationLockUtil.getCreateLock();
        try {
            // 加锁：保证UUID生成+检查的原子性
            createLock.lock();
            do {
                uid = StringUtil.getUUid();
                // 使用exists检查是否存在，效率高于count()
                boolean exists = conversationService.query().eq("id", uid).exists();
                if (!exists) {
                    break;
                }
                retryCount++;
                if (retryCount >= 10){
                    return Result.error("创建会话失败，当前已满不方便创建联系管理员");
                }
            } while (retryCount < 10);

            // 保存会话（加锁状态下执行，确保UUID唯一）
            boolean re = conversationService.save(new ConversationEntity(uid,conversationEntity.getName(), conversationEntity.getType(), standardization));
            if(re){
                String userIdStr = userId.toString();
                List<ConversationVo> conversationListSelf = conversationService.getConversationListSelf(userIdStr);

                // 更新redis缓存
                 redisTemplate.opsForValue().set("conversation:" + userId+":"+username, conversationListSelf);
                redisTemplate.expire("conversation:" + userId+":"+username, 1, TimeUnit.DAYS);
                return Result.ok("创建会话成功");
            }
            return Result.error("创建会话失败");
        }finally {
            // 释放锁（必须在finally中执行，避免死锁）
            createLock.unlock();
        }
    }


    /**
     * 加入会话
     * @param conversationId 目标会话ID
     * @param conversationId
     */
    @PostMapping("/join")
    public Result<?> joinConversation(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("id") String conversationId
    ) {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(7);

        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }

        Long userId = JwtUtils.parseToken(token).get("userId", Long.class);
        Integer userIntId = Math.toIntExact(userId);

        // 校验会话是否是群聊不是退出无法加入
        ConversationEntity con = conversationService.getById(conversationId);
        if (con == null || con.getType() != 1) {
            return Result.error("加入失败，请检查会话ID是否正确");
        }

        // 1. 手动开启事务
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        ReentrantLock memberLock = ConversationLockUtil.getMemberLock(conversationId);
        try {
            memberLock.lock(); // 加锁

            // 2. 业务逻辑（查询→修改→更新）
            ConversationEntity conversation = conversationService.getById(conversationId);

            // ... 处理成员列表 ...
            Set<Integer> split = StringUtil.split(conversation.getConversation());
            split.add(userIntId);
            String standardization = StringUtil.standardization(split);
            conversation.setConversation(standardization);

            conversationService.updateById(conversation);

            // 3. 手动提交事务（提交后再释放锁）
            transactionManager.commit(status);
            String userIdStr = userId.toString();
            List<ConversationVo> conversationListSelf = conversationService.getConversationListSelf(userIdStr);

            // 更新redis缓存
            redisTemplate.opsForValue().set("conversation:" + userId+":"+username, conversationListSelf);
            redisTemplate.expire("conversation:" + userId+":"+username, 1, TimeUnit.DAYS);
            return Result.ok("加入成功");
        } catch (Exception e) {
            transactionManager.rollback(status); // 异常回滚
            return Result.error("失败");
        } finally {
            memberLock.unlock(); // 事务提交/回滚后再释放锁
        }
    }


    /**
     * 退出会话
     * @param conversationId 目标会话ID
     * @param authorizationHeader
     */
    @PostMapping("/leave")
    public Result<?> leaveConversation(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("id") String conversationId
    ) {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(7);

        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }

        Long userId = JwtUtils.parseToken(token).get("userId", Long.class);
        Integer userIntId = Math.toIntExact(userId);

        // 1. 手动开启事务
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        ReentrantLock memberLock = ConversationLockUtil.getMemberLock(conversationId);
        try {
            memberLock.lock();// 加锁

            // 2. 业务逻辑（查询→修改→更新）
            ConversationEntity conversation = conversationService.getById(conversationId);

            // ... 处理成员列表 ...
            Set<Integer> split = StringUtil.split(conversation.getConversation());
            split.remove(userIntId);
            String standardization = StringUtil.standardization(split);
            conversation.setConversation(standardization);

            conversationService.updateById(conversation);

            // 3. 手动提交事务（提交后再释放锁）
            transactionManager.commit(status);

            String userIdStr = userId.toString();
            List<ConversationVo> conversationListSelf = conversationService.getConversationListSelf(userIdStr);

            // 更新redis缓存
            redisTemplate.opsForValue().set("conversation:" + userId+":"+username, conversationListSelf);
            redisTemplate.expire("conversation:" + userId+":"+username, 1, TimeUnit.DAYS);
            return Result.ok("退出成功");
        } catch (Exception e){
            transactionManager.rollback(status);
            return Result.error("失败");
        } finally {
            memberLock.unlock();// 事务提交/回滚后再释放锁
        }
    }


    /**
     * 获取会话自己的会话
     * @param authorizationHeader
     */
    @GetMapping("/getself")
    public Result<?> getSelfConversation(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(7);

        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }

        Long userId = JwtUtils.parseToken(token).get("userId", Long.class);
        Integer userIntId = Math.toIntExact(userId);
        String userIdStr = userIntId.toString(); // 转换为字符串，用于模糊查询

        // 先查询redis缓存
        List<ConversationVo> cachedConversationList = (List<ConversationVo>) redisTemplate.opsForValue().get("conversation:" + userId+":"+username);
        if(cachedConversationList != null){
            redisTemplate.expire("conversation:" + userId+":"+username, 1, TimeUnit.DAYS);
            return Result.ok(cachedConversationList);
        }

        List<ConversationVo> conversationListSelf = conversationService.getConversationListSelf(userIdStr);
        conversationListSelf.forEach(System.out::println);

        // 存入redis
        redisTemplate.opsForValue().set("conversation:" + userId+":"+username, conversationListSelf);
        redisTemplate.expire("conversation:" + userId+":"+username, 1, TimeUnit.DAYS);

        // 返回会话列表（若没有参与任何会话，返回空列表而非错误）
        return Result.ok(conversationListSelf);
    }

    /**
     * 查询会话
     * @param authorizationHeader
     */
    @GetMapping("/select")
    public Result<?> selectConversation(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "key",required = false) String key
    ) {
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }

        Page<ConversationVo> re = conversationService.getConversationList(pageNum, pageSize, key);
        return Result.ok(re);
    }

    /**
     * 随机100个会话数据用来做实时查询
     */
    @GetMapping("/realtime")
    public Result<?> realtimeConversation(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }
        List<ConversationVo> RealTimelist = (List<ConversationVo>) redisTemplate.opsForValue().get("conversation:RealTime");
        if(RealTimelist != null){
            redisTemplate.expire("conversation:RealTime", 1, TimeUnit.DAYS);
            return Result.ok(RealTimelist);
        }
        return Result.error("获取缓存数据失败");
    }
}
