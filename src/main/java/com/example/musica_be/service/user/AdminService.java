package com.example.musica_be.service.user;

import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

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
}
