package com.example.musica_be.dto.payment;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentCancelableResponseDto {
  private String status;
  private String message;
  private boolean is_cancelable;
}
