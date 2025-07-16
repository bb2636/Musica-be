package com.example.musica_be.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtils {
  private static final String SECRET = "this-is-a-very-secure-key-that-is-at-least-64-bytes-long-1234567890!";
  private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
//  private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600_000;
  private static final long ACCESS_TOKEN_EXPIRATION_TIME = 360000000;

  public static Long extractUserId(String jwtWithBearer) {
    try {
      String token = jwtWithBearer.startsWith("Bearer ") ? jwtWithBearer.substring(7) : jwtWithBearer;
      return Long.parseLong(getUserIdFromToken(token));
    } catch (Exception e) {
      throw new RuntimeException("JWT에서 userId 파싱 실패", e);
    }
  }

  // "Bearer {token}" 형식의 JWT 문자열에서 role(String)을 추출하는 유틸 메서드
  public static String extractRole(String jwtWithBearer) {
    try {
      String token = jwtWithBearer.startsWith("Bearer ") ? jwtWithBearer.substring(7) : jwtWithBearer;
      return getRoleFromToken(token);
    } catch (Exception e) {
      throw new RuntimeException("JWT에서 role 파싱 실패", e);
    }
  }

  public static String generateAccessToken(String email, String userId, String role, String name) {
    // UTF-8 safe
    String safeName = new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("role", role)
            .claim("name", safeName)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
            .compact();
  }

  public static String getUserIdFromToken(String token) {
    token = token.startsWith("Bearer ") ? token.substring(7) : token;
    return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }

  public static String getRoleFromToken(String token) {
    token = token.startsWith("Bearer ") ? token.substring(7) : token;
    return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("role", String.class);
  }

  public static String getEmailFromToken(String token) {
    token = token.startsWith("Bearer ") ? token.substring(7) : token;
    return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("email", String.class);
  }

  public static String getNameFromToken(String token) {
    token = token.startsWith("Bearer ") ? token.substring(7) : token;
    return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("name", String.class);
  }

  public static String generateRefreshToken(String email, String userId, String role, String name) {
    long refreshExpiration = 7 * 24 * 3600_000; //7일
    return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("role", role)
            .claim("name", name)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
            .compact();
  }

  public static String refreshAccessToken(String refreshToken) {
    String email = getEmailFromToken(refreshToken);
    String userId = getUserIdFromToken(refreshToken);
    String role = getRoleFromToken(refreshToken);
    String name = getNameFromToken(refreshToken);
    return generateAccessToken(email, userId, role, name);
  }
  //토큰 유효성 검증해 BOOLEAN값 반환
  public static boolean validateToken(String token) {
    try {
      token = token.startsWith("Bearer ") ? token.substring(7) : token;
      Jwts.parserBuilder()
              .setSigningKey(SECRET_KEY)
              .build()
              .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      System.out.println("Token validation failed: " + e.getMessage());
      return false;
    }
  }
  //  만료/유효/잘못된 토큰 구분해 상태 반환
  public static String validateAndGetStatus(String token) {
    try {
      token = token.startsWith("Bearer ") ? token.substring(7) : token;
      Jwts.parserBuilder()
              .setSigningKey(SECRET_KEY)
              .build()
              .parseClaimsJws(token);
      return "VALID";
    } catch (ExpiredJwtException e) {
      System.out.println("Token expired: " + e.getMessage());
      return "EXPIRED";
    } catch (JwtException | IllegalArgumentException e) {
      System.out.println("Invalid token: " + e.getMessage());
      return "INVALID";
    }
  }
  public static Date getExpirationFromToken(String token) {
    token = token.startsWith("Bearer ") ? token.substring(7) : token;
    return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
  }
}
