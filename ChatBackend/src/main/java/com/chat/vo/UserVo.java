package com.chat.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
public class UserVo {
    private Integer id;// 用户id
    private String username;// 用户名
    private String name;// 昵称
    private Integer sex;// 性别 0:男 1:女 2:未知
}
