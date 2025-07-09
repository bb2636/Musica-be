package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.classes.Classes;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PaymentItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @ManyToOne
  @JoinColumn(name = "classes_id", nullable = false)
  private Classes classes;

  private int quantity;
}
