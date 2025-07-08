package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class paymentDetailDto {
  Long reservation_id;
  int reservationAmount;
  LocalDate paid_at;

  List<>
}
