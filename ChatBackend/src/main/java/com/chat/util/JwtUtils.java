package com.chat.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET = "my-secret-key-for-jwt-should-be-long-enough-2025";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

//    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7; // 过期7天
        private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 1天
//    private static final long EXPIRATION_TIME = 1000L; // 1秒
    private static final long REFRESH_THRESHOLD = 1000L * 60 * 60 * 1;  // 小于1个小时刷新

    /**
     * 生成 token
     */
    public static String generateToken(String username, Long userId,String name, LocalDateTime createdAt) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("name", name)
                .claim("createdAt", createdAt.toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ 保留旧方法：仅解析 username
     */
    public static String parseUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * ✅ 新增方法：返回完整 claims
     */
    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断是否快要过期（小于 24 小时）
     */
    public static boolean isTokenExpiringSoon(Claims claims) {
        long now = System.currentTimeMillis();
        long exp = claims.getExpiration().getTime();
        return (exp - now) < REFRESH_THRESHOLD;
    }

    /**
     * 刷新 token
     */
    public static String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (isTokenExpiringSoon(claims)) {
            return generateToken(claims.getSubject(), claims.get("userId", Long.class), claims.get("name", String.class), LocalDateTime.parse(claims.get("createdAt", String.class)));
        }
        return token;
    }

    /**
     * 判断是否已过期（建议捕获异常使用）
     */
    public static boolean isTokenExpired(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
    public static void main(String[] args) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Bearer "+generateToken("root", 1L, "智哥", now));

        long nowMillis = System.currentTimeMillis();
        Date now1 = new Date(nowMillis);
        Date exp = new Date(nowMillis + EXPIRATION_TIME);

        LocalDateTime createdAt= LocalDateTime.now();

        String token = Jwts.builder()
                .setSubject("智哥")
                .claim("userId", "1")
                .claim("name", "1")
                .claim("createdAt", createdAt.toString())
                .setIssuedAt(now1)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        System.out.println("token: "+token);

        Claims body = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        System.out.println("解析数据："+ body);
        System.out.println("解析数据："+ body.get("userId"));
        System.out.println("解析数据："+ body.get("name"));
        System.out.println("解析数据："+ body.get("createdAt"));
        System.out.println("解析数据："+ body.getSubject());
    }

}

