package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class  PaymentDetailDto {
  private Long  payment_id;
  private int paymentAmount;
  private LocalDate paid_at;
  private List< PaymentItemDto> items;
}
