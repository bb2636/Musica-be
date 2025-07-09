package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PaymentStatusUpdateRequest {
  private Long cart_id;
  private String status_update_trigger;
}
