package com.chat.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@ToString
@AllArgsConstructor
@Data
public class FileQuery {
    private String filename;// 文件名关键词
    private LocalDateTime startTime;// 前置时间
    private LocalDateTime endTime;// 后置时间
    private Boolean isNum;// 是否按照数量大小排序
}
//{
//    "filename": "原神",    // 文件名关键词（模糊查询，如查包含“原神”的文件）
//    "startTime": "2025-10-01T00:00:00",  // 开始时间（上传时间≥这个时间）
//    "endTime": "2025-10-19T23:59:59",    // 结束时间（上传时间≤这个时间）
//    "isNum": true        // 是否按数量排序（true=按数量排，false=不按数量排，根据业务需求传）
//}