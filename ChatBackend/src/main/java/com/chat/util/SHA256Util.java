package com.chat.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256Util {
    /**
     * SHA-256 哈希加密
     * @param text 待加密字符串
     * @return 256位哈希的十六进制字符串（64个字符）
     */
    public static String encrypt(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes());

            // 转换为十六进制字符串
            StringBuilder hexStr = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexStr.append('0');
                hexStr.append(hex);
            }
            return hexStr.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    /**
     * SHA-256 加密并转Base64（可选，用于数据传输）
     */
    public static String encryptToBase64(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    public static void main(String[] args) {
        String password = "password123";
        String sha256Hex = encrypt(password);
        String sha256Base64 = encryptToBase64(password);

        System.out.println("SHA-256 十六进制结果：" + sha256Hex);
        System.out.println("SHA-256 Base64结果：" + sha256Base64);
    }
}
