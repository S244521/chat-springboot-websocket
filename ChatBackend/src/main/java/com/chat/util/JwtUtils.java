package com.chat.util;

import com.chat.common.Result;
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

    /**
     * 校验
     */
    public static String verifyToken(String authorizationHeader) {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return "Authorization 头格式错误";
        }
        String token = authorizationHeader.substring(7);

        if (JwtUtils.isTokenExpired(token)) {
            return "token 已过期，请重新登录"; // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return "token 无效";
        }
        return null;
    }
    public static void main(String[] args) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        String token = JwtUtils.generateToken("root", 1L,"伤", now);

        System.out.println("Shang "+token);
    }

}

