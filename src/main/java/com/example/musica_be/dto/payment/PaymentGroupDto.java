package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentGroupDto {
  private Long paymentId;
  private int totalAmount;
  private LocalDateTime paidAt;
  private String status;
  private List<EnrolledClassItemDto> paymentItems;
}
