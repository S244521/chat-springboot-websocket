package com.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 用户id
    private String username;// 用户名
    private String password;// 密码
    private String name;// 昵称
    private Integer sex;// 性别 0:男 1:女 2:未知
}
