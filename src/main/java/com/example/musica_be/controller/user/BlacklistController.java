package com.example.musica_be.controller.user;

import com.example.musica_be.service.user.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    // 🚀 AccessToken 블랙리스트에 등록
    @PostMapping("/add")
    public ResponseEntity<String> addToBlacklist(@RequestParam String accessToken) {
        // JWT 만료 시간 파싱해서 등록하면 더 좋음. 예제는 1시간 후로 고정
        blacklistService.blacklistAccessToken(accessToken, LocalDateTime.now().plusHours(1));
        return ResponseEntity.ok("Token added to blacklist");
    }

    // 🚀 블랙리스트 여부 조회
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkBlacklist(@RequestParam String accessToken) {
        boolean isBlacklisted = blacklistService.isBlacklisted(accessToken);
        return ResponseEntity.ok(isBlacklisted);
    }
}
