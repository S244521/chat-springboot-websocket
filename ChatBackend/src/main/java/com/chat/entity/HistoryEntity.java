package com.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("history")
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
public class HistoryEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 聊天记录id
    private Integer userid;// 用户id
    private Integer conversationid;// 会话id
    private String text;// 聊天记录
    private LocalDateTime createtime;// 发送时间
}
