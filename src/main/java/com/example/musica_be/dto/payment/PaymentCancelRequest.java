package com.example.musica_be.dto.payment;

import java.util.List;

public class PaymentCancelRequest {
  Long  Payment_id;
  List<PaymentItemIdsDto> payment_item_ids;
}
