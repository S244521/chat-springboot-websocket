package com.chat.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
@Data
public class ConversationVo {
    private String id;// 会话机制id
    private String name;// 会话名称
    private Integer type;// 会话类别 0:私聊 1:群聊
    private UserVo user;// 私聊时的会话人
    private List<UserVo> users;// 群聊时的会话人列表
}

