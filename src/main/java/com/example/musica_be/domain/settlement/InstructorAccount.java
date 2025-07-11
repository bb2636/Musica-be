package com.example.musica_be.domain.settlement;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
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
