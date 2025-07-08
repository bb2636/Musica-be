package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.classes.Classes;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int id;

  @ManyToOne
  @JoinColumn(name = "class_id", nullable = false)
  Classes classes;

  @ManyToOne
  @JoinColumn(name = "pay_type_id", nullable = false)
  ReservationType pay_type_id;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  ReservationStatus status_id;

  int amount;
  String payment_method;
  LocalDateTime paid_at;
  LocalDateTime created_at;
}
