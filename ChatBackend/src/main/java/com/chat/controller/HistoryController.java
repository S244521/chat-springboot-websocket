package com.chat.controller;


import com.chat.common.Result;
import com.chat.entity.HistoryEntity;
import com.chat.service.HistoryService;
import com.chat.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * 获取历史记录按时间倒序
     * @param map 会话id
     * @return 历史记录
     */
    @GetMapping("/getHistoryByConversationId")
    private Result<?> getHistoryByConversationId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String,String> map
    ){
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }
        String conversationId = map.get("conversationId");
        List<HistoryEntity> list = historyService.query().eq("conversationid", conversationId).orderByDesc("time").list();

        return Result.ok(list);
    }
}
