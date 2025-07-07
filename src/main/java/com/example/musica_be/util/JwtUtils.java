package com.example.musica_be.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {

  // 안전한 서명 키 생성 (512비트 이상)
  private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);  // 안전한 512 비트 이상의 서명 키
  private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600_000;  // 1시간 (밀리초)
  private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 3600_000;  // 7일 (밀리초)

  // 액세스 토큰 생성
  public static String generateAccessToken(String email, String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .claim("email", email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
        .signWith(SECRET_KEY)
        .compact();
  }
  // 리프레시 토큰 생성
  public static String generateRefreshToken(String email, String userId) {
    return generateAccessToken( email, userId);
  }

  // 토큰에서 이메일 추출
  public static String getEmailFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.get("email", String.class);
  }
  // 토큰에서 userId 추출
  public static String getUserIdFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // 리프레시 토큰을 이용하여 액세스 토큰 갱신
  public static String refreshAccessToken(String refreshToken) {
    String email = getEmailFromToken(refreshToken);
    String user_id = getUserIdFromToken(refreshToken);
    return generateAccessToken(email, user_id);
  }

  // 토큰이 만료되었는지 확인
  public static boolean isTokenExpired(String token) {
    Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  // 토큰에서 만료 시간 추출
  private static Date getExpirationDateFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.getExpiration();
  }
}
