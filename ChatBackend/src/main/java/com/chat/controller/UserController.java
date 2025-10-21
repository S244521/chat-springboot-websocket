package com.chat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.Vo.UserVo;
import com.chat.entity.UserEntity;
import com.chat.service.UserService;
import com.chat.common.Result;
import com.chat.util.JwtUtils;
import com.chat.util.SHA256Util;
import com.chat.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody UserEntity user) {
        String username =StringUtil.xssFilter(user.getUsername());
        String password =SHA256Util.encrypt(StringUtil.xssFilter(user.getPassword()));
        UserEntity userEntity = userService.query().eq("username", username).eq("password", password).one();
        if (userEntity != null){
            LocalDateTime now = LocalDateTime.now();
            String token = JwtUtils.generateToken(username, Long.valueOf(userEntity.getId()),userEntity.getName(), now);
            return Result.ok(
                Map.of("username", username,
                        "name", userEntity.getName(),
                        "sex",userEntity.getSex(),
                        "token", token)
            );
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/sign")
    public Result<?> register(@RequestBody UserEntity user) {
        UserEntity userEntity = userService.query().eq("username", user.getUsername()).one();
        if (userEntity != null){
            return Result.error( "用户已存在");
        }
        user.setUsername(StringUtil.xssFilter(user.getUsername()));
        user.setPassword(SHA256Util.encrypt(user.getPassword()));
        user.setName(StringUtil.xssFilter(user.getName()));
        boolean re = userService.save(user);
        if (re) {
            LocalDateTime now = LocalDateTime.now();
            String token = JwtUtils.generateToken(user.getUsername(), Long.valueOf(user.getId()),user.getName(), now);
            // 这个是使用JWT不需要 存储登录令牌到redis
            return Result.ok(
                Map.of("username", user.getUsername(),
                        "name", user.getName(),
                        "sex",user.getSex(),
                        "token", token)
            );
        }
        return Result.error("注册失败");
    }

    /**
     * 修改信息
     * @param user
     */
    @PostMapping("/update")
    public Result<?> update(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UserEntity user)
    {
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }

        String username = StringUtil.xssFilter(user.getUsername());
        String password = SHA256Util.encrypt(StringUtil.xssFilter(user.getPassword()));
        String name = StringUtil.xssFilter(user.getName());

        boolean re = userService.updateById(new UserEntity(user.getId(), username, password, name, user.getSex()));
        if(re){
            LocalDateTime now = LocalDateTime.now();
            String token = JwtUtils.generateToken(username, Long.valueOf(user.getId()),name, now);
            return Result.ok(
                    Map.of("username", username,
                            "name", name,
                            "sex",user.getSex(),
                            "token", token)
            );
        }
        return Result.error("修改失败");
    }


    /**
     * 注销账号
     * @param authorizationHeader
     */
    @GetMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
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
        boolean re = userService.removeById(userId);
        if(re){
            return Result.ok("注销成功");
        }
        return Result.error("注销失败");
    }

    /**
     * 查询用户
     * @param authorizationHeader
     * @param pageNum
     * @param pageSize
     */
    @GetMapping("/selectUser")
    public Result<?> selectUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(value = "Shang", required = false) String Shang,
            // 接收分页参数，默认页码1，每页10条
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "key", required = false, defaultValue = "") String key
    ){
        if (Shang != null){
            authorizationHeader = "Bearer "+authorizationHeader.substring(6);
        }
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }

        Page<UserVo> allUser = userService.getAllUser(pageNum, pageSize, key, null);
        return Result.ok(allUser);
    }

}
