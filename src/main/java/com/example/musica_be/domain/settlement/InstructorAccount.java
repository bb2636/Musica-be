package com.example.musica_be.domain.settlement;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;
  @OneToOne
  @JoinColumn(name = "instructor_id", nullable = false)
  User user;

  String bank_name;
  String account_number;
  String account_holder_name;

  LocalDateTime created_at;
}
