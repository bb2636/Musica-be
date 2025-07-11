package com.example.musica_be.dto.settlement.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponseDto {
  String bankName;
  String accountNumber;
  String account_holder;
}
