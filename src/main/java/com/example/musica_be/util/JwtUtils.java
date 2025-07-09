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

  // "Bearer {token}" 형식의 JWT 문자열에서 userId(Long)를 추출하는 유틸 메서드
  public static Long extractUserId(String jwtWithBearer) {
    String token = jwtWithBearer.startsWith("Bearer ") ? jwtWithBearer.substring(7) : jwtWithBearer;
    return Long.parseLong(getUserIdFromToken(token));
  }

  // 액세스 토큰 생성 (role 추가)
  public static String generateAccessToken(String email, String userId, String role) {
    return Jwts.builder()
            .setSubject(userId)  // userId를 subject로 설정
            .claim("email", email)
            .claim("role", role)  // role을 클레임으로 추가
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
            .signWith(SECRET_KEY)
            .compact();
  }

  // 리프레시 토큰 생성 (role 추가)
  public static String generateRefreshToken(String email, String userId, String role) {
    return generateAccessToken(email, userId, role);  // 리프레시 토큰도 액세스 토큰처럼 처리
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
  // 토큰에서 role 추출
  public static String getRoleFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.get("role", String.class);
  }

  // 리프레시 토큰을 이용하여 액세스 토큰 갱신 (role 포함)
  public static String refreshAccessToken(String refreshToken) {
    // 이메일, 사용자 ID, 역할 정보 추출
    String email = getEmailFromToken(refreshToken);
    String userId = getUserIdFromToken(refreshToken);
    String role = getRoleFromToken(refreshToken);  // role 정보 추출

    // 액세스 토큰 갱신 시 role 정보도 함께 포함
    return generateAccessToken(email, userId, role);  // role을 포함하여 새로운 액세스 토큰 생성
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
