package com.example.musica_be.domain.cart;

import com.example.musica_be.domain.classes.Classes;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "classes_id", nullable = false)
  private Classes classes;

  private Long quantity;
  private LocalDateTime added_at;
}
