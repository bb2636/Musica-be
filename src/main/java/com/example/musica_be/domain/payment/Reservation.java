package com.example.musica_be.domain.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int id;
  int class_id;
  int pay_type_id;
  int status_id;

  int amount;
  String payment_method;
  Timestamp paid_at;
  Timestamp created_at;
}
