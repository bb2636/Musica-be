package com.example.musica_be.service.user;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.SocialAccount;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.question.QuestionDto;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UpdateUserReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.repository.qna.QuestionRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.SocialAccountRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.repository.wishlist.WishlistRepository;
import com.example.musica_be.util.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LevelRepository levelRepository;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountRepository socialAccountRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;

    // 이메일로 사용자 찾기
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 사용자 저장 (새로운 사용자 또는 수정된 사용자)
    public User save(User user) {
        return userRepository.save(user);
    }

    // 기존 registerUser() 내부에 있던 중복 검사 분리
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 소셜 계정 연결
    @Transactional
    public void connectSocialAccount(String socialId, String provider, User user) {
        // 소셜 계정이 이미 연결된 경우 확인
        Optional<SocialAccount> existingAccount = socialAccountRepository.findBySocialIdAndProvider(socialId, provider);
        if (existingAccount.isPresent()) return; // 이미 연결된 소셜 계정이 있으면 추가로 처리하지 않음

        // 새로운 소셜 계정 연결
        SocialAccount socialAccount = SocialAccount.builder()
                .socialId(socialId)
                .provider(provider)
                .user(user)
                .build();

        // 소셜 계정 저장
        socialAccountRepository.save(socialAccount);
    }

    // 기존 사용자 등록
    public UserResDto registerUser(RegisterReqDto registerReqDto) {
        // 이메일 중복 체크
        if (isEmailDuplicate(registerReqDto.getEmail())) {
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

        // 사용자 정보 설정
        User user = new User();
        user.setName(registerReqDto.getName());
        user.setEmail(registerReqDto.getEmail());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 저장
        user.setRole(Role.valueOf(registerReqDto.getRole()));
        user.setLevel(level); //(Beginner, Intermediate, Advanced)
        user.setCreatedAt(LocalDateTime.now());
        user.setIsApproved(!registerReqDto.getRole().equals("INSTRUCTOR")); // 기본적으로 INSTRUCTOR는 승인되지 않음

        User savedUser = userRepository.save(user);
        return new UserResDto(savedUser); // UserResDto로 변환하여 응답
    }

    // OAuth2 로그인 후 받은 정보로 회원가입 처리
    public UserResDto registerUserFromOAuth(String email, String name, String role, Long levelId) {
        Level level = null;

        // levelId 못받으면 자동으로 1L
        try {
            level = levelRepository.findById(levelId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid levelId"));
        } catch (Exception e) {
            level = levelRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Default level not found"));
        }

        Role userRole;
        try {
            userRole = Role.valueOf(role);
        } catch (Exception e) {
            userRole = Role.USER;
        }

        // 이미 존재하면 그대로 반환
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return new UserResDto(existingUser.get());
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(userRole);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsApproved(true);

        // ✅ 소셜 회원가입이므로 더미 비밀번호 입력
        user.setPassword("social_login");

        if (userRole == Role.USER) {
            user.setLevel(level);
        }

        userRepository.save(user);
        return new UserResDto(user);
    }


    // 로그인
    public Map<String, String> login(LoginReqDto loginReqDto) {
        Optional<User> userOpt = userRepository.findByEmail(loginReqDto.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 비밀번호 검증
            if (passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
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
                // 토큰을 반환
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
    }

    // 회원 정보 수정
    @Transactional
    public UserResDto updateUserInfo(Long userId, UpdateUserReqDto updateUserReqDto) {
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
    // 수강 중인 강의 목록 조회
//    public List<Enrollment> getCurrentEnrollments(Long userId) {
//        return enrollmentRepository.findByUserIdAndStatus(userId, "ENROLLED");
//    }
//
    // 결제 내역 조회
//    public List<Payment> getPaymentHistory(Long userId) {
//        return paymentRepository.findByUserId(userId);
//    }

    // 찜 목록 조회
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // 후기 목록 조회
    public List<Review> getReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    // 내가 등록한 질문 목록 조회
    public List<QuestionDto> getUserQuestions(Long userId) {
        return questionRepository.findByUserId(userId)
                .stream()
                .map(question -> QuestionDto.builder()
                        .questionId(question.getId())
                        .classId(question.getLecture().getClasses().getId())
                        .userId(question.getUser().getId())
                        .question(question.getQuestion())
                        .createdAt(question.getCreatedAt())
                        .build())
                .toList();
    }
    public Level getLevelById(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기본 레벨을 찾을 수 없습니다."));
    }
}