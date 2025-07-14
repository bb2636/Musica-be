package com.example.musica_be.repository.settlement;

import com.example.musica_be.domain.settlement.InstructorAccount;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorAccountRepository extends JpaRepository<InstructorAccount, Long> {
  Optional<InstructorAccount> findByUser(User instructor);
}
