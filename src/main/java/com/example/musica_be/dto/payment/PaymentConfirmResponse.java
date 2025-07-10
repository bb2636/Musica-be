package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import javax.smartcardio.Card;

@Data
@Builder
public class PaymentConfirmResponse {
  private String paymentKey;
  private String orderId;
  private String orderName;
  private String status;
  private String method;
  private String approvedAt;
  private Card card;
  private int totalAmount;
  private int vat;
}
