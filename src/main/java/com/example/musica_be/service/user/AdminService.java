package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.ApprovalStatus;
import com.example.musica_be.domain.user.RefreshToken;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.repository.user.RefreshTokenRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdminUser() {
        if (!userRepository.existsByEmail("admin@musica.com")) {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("admin@musica.com");
            adminUser.setPassword(passwordEncoder.encode("adminPassword"));
            adminUser.setRole(Role.ADMIN);
            adminUser.setIsApproved(true);
            adminUser.setApprovalStatus(ApprovalStatus.APPROVED);
            adminUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(adminUser);
        }
    }

    public Map<String, String> adminLogin(LoginReqDto loginReqDto) {
        User user = userRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admin can log in");
        }

        if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String accessToken = JwtUtils.generateAccessToken(
                user.getEmail(), String.valueOf(user.getId()),
                user.getRole().name(), user.getName()
        );
        String refreshToken = JwtUtils.generateRefreshToken(
                user.getEmail(), String.valueOf(user.getId()),
                user.getRole().name(), user.getName()
        );

        // ✅ 관리자도 refreshToken 저장
        refreshTokenRepository.save(new RefreshToken(user, refreshToken));

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    public List<User> getAllInstructors() {
        return userRepository.findByRole(Role.INSTRUCTOR);
    }

    public List<User> getPendingInstructors() {
        return userRepository.findByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.PENDING);
    }

    public void approveInstructor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new IllegalArgumentException("Only instructors can be approved");
        }
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setIsApproved(true);
        userRepository.save(user);
    }

    public void rejectInstructor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new IllegalArgumentException("Only instructors can be rejected");
        }
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setIsApproved(false);
        userRepository.save(user);
    }
}
