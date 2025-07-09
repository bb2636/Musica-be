package com.example.musica_be.dto.payment;

import java.util.List;

public class PaymentCancelRequest {
  Long reservation_id;
  List<ReservationItemIdsDto> reservation_item_ids;
}
