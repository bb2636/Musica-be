package com.example.musica_be.dto.payment;

import lombok.Data;

import java.util.List;

@Data
public class CancelPaymentRequestDto {
  private Long payment_id;
  private List<Long> payment_item_ids;
}
