package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name= "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "class_id", nullable = false)
  private Classes classes;

  @ManyToOne
  @JoinColumn(name = "pay_type_id", nullable = false)
  private PaymentType pay_type_id;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private PaymentStatus status_id;

  private int amount;
  private String payment_method;
  private LocalDateTime paid_at;
  private LocalDateTime created_at;
}
