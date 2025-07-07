package com.example.musica_be.repository.user;

import com.example.musica_be.domain.user.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findBySocialIdAndProvider(String socialId, String provider);
}
