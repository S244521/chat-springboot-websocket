package com.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("root")
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
public class RootEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 管理员id
    private String username;// 用户名
    private String password;// 密码
}
