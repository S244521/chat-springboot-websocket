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
@TableName("file")
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中未知的字段
public class FileEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;// 文件资源id
    private String filename;// 文件名
    private String fileurl;// 文件url
    private LocalDateTime uploadtime;// 上传时间
    private Integer num;// 下载次数
}
