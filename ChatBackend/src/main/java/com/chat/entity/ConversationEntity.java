package com.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ConversationEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 会话机制id
    private Integer type;// 会话类别 0:私聊 1:群聊
    private String conversation;// 会话人id '1,2,3'
}
