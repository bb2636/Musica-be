package com.example.musica_be.dto.payment;

import java.sql.Timestamp;

public class paymentResponseDto {
  private Long paymentId;
  private Long orderId;
  private String status;
  private Long paidAmount;
  private String paymentMethod;
  private Timestamp approvedAt;
  private String message;
}
