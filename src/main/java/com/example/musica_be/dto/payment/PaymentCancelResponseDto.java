package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCancelResponseDto {
  private String paymentKey;
  private String orderId;
  private String status;
  private String canceledAt;
  private int cancelAmount;
  private String cancelReason;
}