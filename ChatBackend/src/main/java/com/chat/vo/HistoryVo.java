package com.chat.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
public class HistoryVo {
    private Integer id;// 聊天记录id
    private String username;// 用户id
    private String text;// 聊天记录
    private LocalDateTime createtime;// 发送时间
}
