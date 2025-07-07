package com.example.musica_be.domain.payment;

import com.example.musica_be.domain.classes.Classes;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.sql.Timestamp;

public class CartItem {
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "classes_id", nullable = false)
  private Classes classes;
  private Long quantity;
  private Timestamp added_at;
}
