package com.chat.controller;


import com.chat.common.Result;
import com.chat.entity.ConversationEntity;
import com.chat.service.ConversationService;
import com.chat.util.JwtUtils;
import com.chat.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

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

        Long userId = JwtUtils.parseToken(token).get("userId", Long.class);
        Set<Integer> split = StringUtil.split(conversationEntity.getConversation());
        split.add(Math.toIntExact(userId));

        String standardization = StringUtil.standardization(split);

        if(conversationEntity.getType()==0){
            if(split.size()!=2){
                return Result.error("私聊会话 participants 参数错误");
            }
            boolean exists = conversationService.query()
                    .eq("type", 0) // 私聊类型
                    .eq("conversation", standardization) // 用标准化字符串查询
                    .exists();
            if(exists){
                return Result.error("该会话已存在");
            }
        }

        String uid;
        // 最大10次超出显示当前不方便创建
        int retryCount = 0;

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


        boolean re = conversationService.save(new ConversationEntity(uid, conversationEntity.getType(), standardization));
        if(re){
          return Result.ok("创建会话成功");
        }
        return Result.error("创建会话失败");
    }


    /**
     * 加入会话
     */

    /**
     * 退出会话
     */

    /**
     * 获取会话
     */


}
