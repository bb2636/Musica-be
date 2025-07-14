package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 애플리케이션 시작 시 관리자 계정이 없는 경우 미리 생성
    @PostConstruct
    public void initAdminUser() {
        if (!userRepository.existsByEmail("admin@musica.com")) {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("admin@musica.com");
            adminUser.setPassword(passwordEncoder.encode("adminPassword")); // 기본 관리자의 비밀번호
            adminUser.setRole(Role.ADMIN);  // 관리자로 설정
            adminUser.setIsApproved(true);  // 승인된 상태로 설정
            adminUser.setCreatedAt(LocalDateTime.now());

            userRepository.save(adminUser);
        }
    }
    // 관리자 로그인 (관리자만 로그인)
    public Map<String, String> adminLogin(LoginReqDto loginReqDto) {
        Optional<User> userOpt = userRepository.findByEmail(loginReqDto.getEmail());

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // 관리자가 아닌 경우 로그인 불가
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admin can log in");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // 로그인 성공, JWT 토큰 생성
        String accessToken = JwtUtils.generateAccessToken(
                user.getEmail(),
                String.valueOf(user.getId()),
                user.getRole().name(),
                user.getName()
        );
        String refreshToken = JwtUtils.generateRefreshToken(
                user.getEmail(),
                String.valueOf(user.getId()),
                user.getRole().name(),
                user.getName()
        );

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }


    // 강사 목록 조회 (승인 대기 중인 강사들만)
    public List<User> getPendingInstructors() {
        return userRepository.findByRoleAndIsApproved(Role.INSTRUCTOR, false);
    }

    // 강사 승인
    public void approveInstructor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.INSTRUCTOR) {
            user.setIsApproved(true);  // 승인 처리
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Only instructors can be approved");
        }
    }

    // 강사 거절
    public void rejectInstructor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.INSTRUCTOR) {
            user.setIsApproved(false);  // 승인 상태를 false로 설정하여 거절 처리
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Only instructors can be rejected");
        }
    }
}
