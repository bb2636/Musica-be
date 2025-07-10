package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentGroupDto {
  private Long payment_id;
  private int totalAmount;
  private LocalDateTime paid_at;
  private List<EnrolledClassItemDto> classes;
}
