package com.chat.util;

public class StringUtil {
    public static String xssFilter(String str){
        return str.replaceAll("<","&lt;").replaceAll(">","&gt;");
    }

}
