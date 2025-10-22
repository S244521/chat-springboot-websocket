package com.chat.controller;


import com.chat.Vo.HistoryVo;
import com.chat.common.Result;
import com.chat.entity.HistoryEntity;
import com.chat.service.HistoryService;
import com.chat.service.UserService;
import com.chat.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserService userService;

    /**
     * 添加历史记录
     */
    @PostMapping("/addHistory")
    private Result<?> addHistory(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody HistoryEntity historyEntity
    ){
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

        Integer userId = Math.toIntExact(JwtUtils.parseToken(token).get("userId", Long.class));
        historyEntity.setUserid(userId);
        historyEntity.setCreatetime(LocalDateTime.now());
        return historyService.save(historyEntity) ? Result.ok("保存成功") : Result.error("保存失败");
    }

    /**
     * 获取历史记录按时间倒序
     * @param map 会话id
     * @return 历史记录
     */
    @PostMapping("/getHistoryByConversationId")
    private Result<?> getHistoryByConversationId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String,String> map
    ){
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }
        String conversationId = map.get("conversationId");
        List<HistoryEntity> list = historyService.query().eq("conversationid", conversationId).orderByAsc("createtime").list();
        List<HistoryVo> re= list.stream().map(item -> {
            HistoryVo historyVo = new HistoryVo();
            historyVo.setId(item.getId());
            historyVo.setText(item.getText());
            historyVo.setCreatetime(item.getCreatetime());
            historyVo.setCreatetime(item.getCreatetime());
            historyVo.setUsername(userService.getById(item.getUserid()).getName());

            return historyVo;
        }).toList();
        return Result.ok(re);
    }
}
