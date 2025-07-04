package com.example.musica_be.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET_KEY = "secret";  // 이 부분은 실제 환경에서 안전하게 보관해야 합니다.
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600_000;  // 1시간 (밀리초)
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 3600_000;  // 7일 (밀리초)

    // 액세스 토큰 생성
    public static String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // 리프레시 토큰 생성
    public static String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // 토큰에서 이메일 추출
    public static String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 리프레시 토큰을 이용하여 액세스 토큰 갱신
    public static String refreshAccessToken(String refreshToken) {
        String email = getEmailFromToken(refreshToken);
        return generateAccessToken(email);
    }

    // 토큰이 만료되었는지 확인
    public static boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 토큰에서 만료 시간 추출
    private static Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}
