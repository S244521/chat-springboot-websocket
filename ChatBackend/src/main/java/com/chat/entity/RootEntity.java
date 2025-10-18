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
public class RootEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 管理员id
    private String username;// 用户名
    private String password;// 密码
}
