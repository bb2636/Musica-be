package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.BlacklistedAccessToken;
import com.example.musica_be.repository.user.BlacklistedAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistedAccessTokenRepository blacklistRepo;

    public void blacklistAccessToken(String token, LocalDateTime expiredAt) {
        BlacklistedAccessToken blacklisted = BlacklistedAccessToken.builder()
                .accessToken(token)
                .expiredAt(expiredAt)
                .build();
        blacklistRepo.save(blacklisted);
    }

    public boolean isBlacklisted(String token) {
        return blacklistRepo.existsByAccessToken(token);
    }
}
