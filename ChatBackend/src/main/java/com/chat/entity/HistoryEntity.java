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
public class HistoryEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 聊天记录id
    private Integer userid;// 用户id
    private Integer conversationid;// 会话id
    private String text;// 聊天记录
    private LocalDateTime createtime;// 发送时间
}
