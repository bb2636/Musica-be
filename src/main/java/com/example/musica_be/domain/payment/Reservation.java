package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.classes.Classes;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "class_id", nullable = false)
  private Classes classes;

  @ManyToOne
  @JoinColumn(name = "pay_type_id", nullable = false)
  private ReservationType pay_type_id;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private ReservationStatus status_id;

  private int amount;
  private String payment_method;
  private LocalDateTime paid_at;
  private LocalDateTime created_at;
}
