package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UpdateUserReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LevelRepository levelRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResDto registerUser(RegisterReqDto registerReqDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(registerReqDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        // role이 USER일 경우만 levelId를 받을 수 있게 처리
        if ("USER".equalsIgnoreCase(registerReqDto.getRole()) && registerReqDto.getLevelId() == null) {
            throw new IllegalArgumentException("Level must be selected for users.");
        }

        Level level = null;
        if ("USER".equalsIgnoreCase(registerReqDto.getRole())) {
            level = levelRepository.findById(registerReqDto.getLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid level ID"));
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(registerReqDto.getPassword());

        User user = new User();
        user.setName(registerReqDto.getName());
        user.setEmail(registerReqDto.getEmail());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 저장
        user.setRole(Role.valueOf(registerReqDto.getRole()));
        user.setLevel(level); //(Beginner, Intermediate, Advanced)
        user.setCreatedAt(LocalDateTime.now());
        user.setIsApproved(!registerReqDto.getRole().equals("INSTRUCTOR")); // 기본적으로 INSTRUCTOR는 승인되지 않음

        User savedUser = userRepository.save(user);
        return new UserResDto(savedUser);  // UserResDto로 변환하여 응답
    }

    // 로그인
    public Map<String, String> login(LoginReqDto loginReqDto) {
        Optional<User> userOpt = userRepository.findByEmail(loginReqDto.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 비밀번호 검증
            if (passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
                // 로그인 성공, JWT 토큰 생성
                String accessToken = JwtUtils.generateAccessToken(user.getEmail(), String.valueOf(user.getId()));
                String refreshToken = JwtUtils.generateRefreshToken(user.getEmail(), String.valueOf(user.getId()));

                return Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                );
            } else {
                throw new IllegalArgumentException("Invalid password");
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId) {
        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 사용자가 존재하면 사용자 삭제
        userRepository.delete(user);
        // 예약 삭제
//        reservationRepository.deleteByUser(user);
//
//        // 찜 목록 삭제
//        wishlistRepository.deleteByUser(user);
//
//        // 리뷰 삭제
//         reviewRepository.deleteByUser(user);
    }

    // 회원 정보 수정
    @Transactional
    public UserResDto updateUser(Long userId, UpdateUserReqDto updateUserReqDto) {
        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 이메일 중복 체크
        if (!user.getEmail().equals(updateUserReqDto.getEmail()) && userRepository.existsByEmail(updateUserReqDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 레벨 수정 시 유효성 검사 (USER만 레벨을 수정할 수 있음)
        if (updateUserReqDto.getLevelId() != null && !user.getRole().equals(Role.USER)) {
            throw new IllegalArgumentException("레벨은 수강생만 수정 가능합니다.");
        }

        // 이름, 이메일, 레벨 수정
        user.setName(updateUserReqDto.getName());
        user.setEmail(updateUserReqDto.getEmail());

        if (updateUserReqDto.getLevelId() != null) {
            Level level = levelRepository.findById(updateUserReqDto.getLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("잘못된 레벨 ID입니다."));
            user.setLevel(level);
        }

        User updatedUser = userRepository.save(user);
        return new UserResDto(updatedUser);
    }
}