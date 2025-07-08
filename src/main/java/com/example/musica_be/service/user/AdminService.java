package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

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
