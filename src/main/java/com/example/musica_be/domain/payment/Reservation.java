package com.example.musica_be.domain.payment;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private int class_id;
  @ManyToOne
  @JoinColumn(name = "pay_type_id", nullable = false)
  private ReservationType pay_type_id;
  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private ReservationStatus status_id;

  private int amount;
  private String payment_method;
  private Timestamp paid_at;
  private Timestamp created_at;
}
