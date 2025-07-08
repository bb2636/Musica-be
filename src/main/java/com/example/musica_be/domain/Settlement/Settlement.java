package com.example.musica_be.domain.Settlement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Settlement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "settlement_status_id", nullable = false)
  private Settlement settlement_status_id;

  private Long instructor_id;
  private Long total_amount;
  private Long commission_rate;
  private Long net_amount;

  private String settlement_month;
  private LocalDateTime settled_at;
  private LocalDateTime created_at;
}
