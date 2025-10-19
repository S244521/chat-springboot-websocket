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


@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("conversation")
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
@Data
public class ConversationEntity {
    private String id;// 会话机制id
    private Integer type;// 会话类别 0:私聊 1:群聊
    private String conversation;// 会话人id '1,2,3'
}
