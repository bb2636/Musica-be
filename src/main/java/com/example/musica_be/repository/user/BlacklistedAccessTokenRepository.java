package com.example.musica_be.repository.user;

import com.example.musica_be.domain.user.BlacklistedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedAccessTokenRepository extends JpaRepository<BlacklistedAccessToken, Long> {
    boolean existsByAccessToken(String accessToken);
}
