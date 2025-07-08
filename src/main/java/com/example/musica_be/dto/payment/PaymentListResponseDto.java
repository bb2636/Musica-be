package com.example.musica_be.dto.payment;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentListResponseDto {
  private Long reservation_id;
  private String title;
  private int amount;
  private String status;
  private LocalDateTime paid_at;
}
