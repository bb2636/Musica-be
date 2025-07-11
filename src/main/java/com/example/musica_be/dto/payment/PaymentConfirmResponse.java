package com.example.musica_be.dto.payment;

import com.fasterxml.jackson.databind.JsonNode;
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
  private JsonNode card;
  private int totalAmount;
  private int vat;
}
