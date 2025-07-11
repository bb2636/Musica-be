package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PaymentStatusUpdateRequestDto {
  private Long cart_id;
  private String OrderId;
  private int amount;
  private String paymentKey;
  private String pay_method;
}
