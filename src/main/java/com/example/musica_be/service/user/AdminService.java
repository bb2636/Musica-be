package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.InstructorDto;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.domain.user.ApprovalStatus;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 🚀 애플리케이션 시작 시 기본 관리자 계정 자동 생성
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        createDefaultAdminAccount();
    }

    /**
     * 🔧 기본 관리자 계정 생성
     */
    private void createDefaultAdminAccount() {
        String adminEmail = "admin@musica.com";

        try {
            // 기존 관리자 계정 확인
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                log.info("✅ 기본 관리자 계정이 이미 존재합니다: {}", adminEmail);
                return;
            }

            // 관리자 계정 생성
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setName("System Admin");
            adminUser.setPassword(passwordEncoder.encode("adminpassword"));
            adminUser.setRole(Role.ADMIN);
            adminUser.setApprovalStatus(ApprovalStatus.APPROVED);
            adminUser.setIsApproved(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setLevel(null); // 관리자는 레벨 불필요

            userRepository.save(adminUser);

            log.info("🎯 기본 관리자 계정이 생성되었습니다!");
            log.info("📧 이메일: {}", adminEmail);
            log.info("🔑 비밀번호: adminpassword");
            log.info("⚠️  보안을 위해 초기 비밀번호를 변경해주세요!");

        } catch (Exception e) {
            log.error("❌ 기본 관리자 계정 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 🔐 관리자 로그인 처리
     */
    public Map<String, String> adminLogin(LoginReqDto loginReqDto) {
        log.info("관리자 로그인 시도: {}", loginReqDto.getEmail());

        // 사용자 조회
        User user = userRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 관리자 권한 확인
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String accessToken = JwtUtils.generateAccessToken(
                user.getEmail(),
                user.getId().toString(),
                user.getRole().name(),
                user.getName()
        );

        String refreshToken = JwtUtils.generateRefreshToken(
                user.getEmail(),
                user.getId().toString(),
                user.getRole().name(),
                user.getName()
        );

        log.info("관리자 로그인 성공: {}", user.getEmail());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "role", user.getRole().name(),
                "name", user.getName(),
                "email", user.getEmail()
        );
    }

    /**
     * 🎯 모든 강사 목록을 DTO로 반환
     * - 승인됨, 대기중, 거절됨 모든 상태 포함
     */
    public List<InstructorDto> getAllInstructorsDto() {
        log.info("모든 강사 목록 조회 (DTO 변환) 시작");

        try {
            // Repository 메서드가 없다면 기본 메서드 사용
            List<User> instructors;
            try {
                instructors = userRepository.findByRoleOrderByCreatedAtDesc(Role.INSTRUCTOR);
            } catch (Exception e) {
                // 메서드가 없다면 기본 방식 사용
                instructors = userRepository.findByRole(Role.INSTRUCTOR);
                instructors.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }

            List<InstructorDto> instructorDtos = instructors.stream()
                    .map(InstructorDto::from)
                    .collect(Collectors.toList());

            log.info("모든 강사 목록 조회 성공: {}명", instructorDtos.size());
            return instructorDtos;

        } catch (Exception e) {
            log.error("모든 강사 목록 조회 실패", e);
            throw new RuntimeException("강사 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 🎯 승인 대기 중인 강사 목록을 DTO로 반환
     */
    public List<InstructorDto> getPendingInstructorsDto() {
        log.info("승인 대기 강사 목록 조회 (DTO 변환) 시작");

        try {
            // Repository 메서드가 없다면 기본 메서드 사용
            List<User> pendingInstructors;
            try {
                pendingInstructors = userRepository.findByRoleAndApprovalStatusOrderByCreatedAtDesc(
                        Role.INSTRUCTOR, ApprovalStatus.PENDING);
            } catch (Exception e) {
                // 메서드가 없다면 기본 방식 사용
                pendingInstructors = userRepository.findByRoleAndApprovalStatus(
                        Role.INSTRUCTOR, ApprovalStatus.PENDING);
                pendingInstructors.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }

            List<InstructorDto> instructorDtos = pendingInstructors.stream()
                    .map(InstructorDto::from)
                    .collect(Collectors.toList());

            log.info("승인 대기 강사 목록 조회 성공: {}명", instructorDtos.size());
            return instructorDtos;

        } catch (Exception e) {
            log.error("승인 대기 강사 목록 조회 실패", e);
            throw new RuntimeException("승인 대기 강사 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 🔧 강사 승인 처리 (예외 처리 개선)
     */
    @Transactional
    public void approveInstructor(Long userId) {
        log.info("강사 승인 처리 시작: userId={}", userId);

        try {
            User instructor = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

            // 강사 역할 확인
            if (!Role.INSTRUCTOR.equals(instructor.getRole())) {
                throw new IllegalArgumentException("해당 사용자는 강사가 아닙니다.");
            }

            // 이미 처리된 상태인지 확인
            if (ApprovalStatus.APPROVED.equals(instructor.getApprovalStatus())) {
                throw new IllegalStateException("이미 승인된 강사입니다.");
            }

            if (ApprovalStatus.REJECTED.equals(instructor.getApprovalStatus())) {
                throw new IllegalStateException("이미 거절된 강사입니다.");
            }

            // 승인 처리
            instructor.setApprovalStatus(ApprovalStatus.APPROVED);
            instructor.setIsApproved(true);
            userRepository.save(instructor);

            log.info("강사 승인 완료: userId={}, email={}", userId, instructor.getEmail());

        } catch (Exception e) {
            log.error("강사 승인 처리 실패: userId={}", userId, e);
            throw e;
        }
    }

    /**
     * 🔧 강사 거절 처리 (예외 처리 개선)
     */
    @Transactional
    public void rejectInstructor(Long userId) {
        log.info("강사 거절 처리 시작: userId={}", userId);

        try {
            User instructor = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

            // 강사 역할 확인
            if (!Role.INSTRUCTOR.equals(instructor.getRole())) {
                throw new IllegalArgumentException("해당 사용자는 강사가 아닙니다.");
            }

            // 이미 처리된 상태인지 확인
            if (ApprovalStatus.APPROVED.equals(instructor.getApprovalStatus())) {
                throw new IllegalStateException("이미 승인된 강사입니다.");
            }

            if (ApprovalStatus.REJECTED.equals(instructor.getApprovalStatus())) {
                throw new IllegalStateException("이미 거절된 강사입니다.");
            }

            // 거절 처리
            instructor.setApprovalStatus(ApprovalStatus.REJECTED);
            instructor.setIsApproved(false);
            userRepository.save(instructor);

            log.info("강사 거절 완료: userId={}, email={}", userId, instructor.getEmail());

        } catch (Exception e) {
            log.error("강사 거절 처리 실패: userId={}", userId, e);
            throw e;
        }
    }

    // 🔍 기존 메서드들 (User 엔티티 반환) - 하위 호환성을 위해 유지
    public List<User> getAllInstructors() {
        return userRepository.findByRole(Role.INSTRUCTOR);
    }

    public List<User> getPendingInstructors() {
        return userRepository.findByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.PENDING);
    }
}