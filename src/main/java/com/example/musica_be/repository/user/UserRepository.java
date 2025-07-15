package com.example.musica_be.repository.user;

import com.example.musica_be.domain.user.ApprovalStatus;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회
    boolean existsByEmail(String email);

    //관리자용(강사 승인)
    List<User> findByRole(Role role);
    List<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus status);
}