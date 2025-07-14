package com.example.musica_be.domain.payment;

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
  @Column(nullable = false, unique = true)
  private String orderId;
  @Column(nullable = false, unique = true)
  private String paymentKey;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "pay_type_id", nullable = false)
  private PaymentType payType;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private PaymentStatus status;

  private int amount;
  private String paymentMethod;
  private LocalDateTime paidAt;
  private LocalDateTime createdAt;
}
