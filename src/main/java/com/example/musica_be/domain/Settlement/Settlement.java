package com.example.musica_be.domain.Settlement;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity
public class Settlement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int id;
  int settlement_status_id;
  int instructor_id;
  int total_amount;
  int commission_rate;
  int net_amount;
  String settlement_month;
  Timestamp settled_at;
  Timestamp created_at;
}
