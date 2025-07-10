package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelPaymentResponseDto {
  private String status;
  private String message;
  private boolean is_cancelable;
}
