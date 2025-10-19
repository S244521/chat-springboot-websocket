package com.chat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.Vo.UserVo;
import com.chat.common.Result;
import com.chat.entity.RootEntity;
import com.chat.entity.UserEntity;
import com.chat.service.RootService;
import com.chat.service.UserService;
import com.chat.util.JwtUtils;
import com.chat.util.SHA256Util;
import com.chat.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/root")
public class RootController {

    @Autowired
    private UserService userService;

    @Autowired
    private RootService rootService;

    /**
     * 后台登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> user) {
        String username = StringUtil.xssFilter(user.get("username"));
        String password = StringUtil.xssFilter(user.get("password"));
        RootEntity root = rootService.query().eq("username", username).eq("password", password).one();

        if (root != null){
            LocalDateTime now = LocalDateTime.now();
            String token = JwtUtils.generateToken(username, Long.valueOf(root.getId()),"伤", now);
            return Result.ok(
                    Map.of("username", username,
                            "name", "伤",
                            "token", token)
            );
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 删除指定用户
     * @param authorizationHeader
     * @param id
     */
    @PostMapping("/deleteUserById")
    public Result<?> deleteUserById(
            @RequestHeader("root") String authorizationHeader,
            @RequestParam String id
    ){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Shang ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(6);
        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }
        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }
        return userService.removeById(id) ? Result.ok("删除成功") : Result.error("删除失败");
    }
}