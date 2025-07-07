package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Cart {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  private Timestamp created_at;
}
