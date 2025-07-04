package com.example.musica_be.domain.Settlement;

import jakarta.persistence.*;

@Entity
public class SettlementStatus {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String name;
}
