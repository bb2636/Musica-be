package com.example.musica_be.dto.settlement.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountRequestDto {
  String bankName;
  String accountNumber;
  String accountHolder;
}
