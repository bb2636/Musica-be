package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
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
        user.setLevel(level);
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
                // 액세스 토큰과 리프레시 토큰 생성
                String accessToken = JwtUtils.generateAccessToken(user.getEmail());
                String refreshToken = JwtUtils.generateRefreshToken(user.getEmail());

                // 응답에 액세스 토큰과 리프레시 토큰 포함
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);

                return tokens;
            } else {
                throw new IllegalArgumentException("Invalid password");
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
}
