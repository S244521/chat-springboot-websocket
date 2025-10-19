package com.chat.util;

import java.util.*;
import java.util.stream.Collectors;

public class StringUtil {
    public static String xssFilter(String str){
        return str.replaceAll("<","&lt;").replaceAll(">","&gt;");
    }



    // 生成随机UUid字符串32位 去除中间的横线
    public static String getUUid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    // 标准化会话标识
    public static String standardization(Set<Integer> split) {
        List<Integer> sortedIds = new ArrayList<>(split);
        Collections.sort(sortedIds); // 排序后保证顺序一致

        // 3. 标准化会话字符串（如 "1,2"）
        String standardizedConversation = sortedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return standardizedConversation;
    }

    // 字符串解析为数组 '1,2,3'
    public static Set<Integer> split(String str) {
        String[] split1 = str.split(",");
        Set<Integer> split= new HashSet<>();
        for(String s : split1){
            split.add(Integer.parseInt(s));
        }
        return split;
    }

    public static void main(String[] args) {
    }
}
