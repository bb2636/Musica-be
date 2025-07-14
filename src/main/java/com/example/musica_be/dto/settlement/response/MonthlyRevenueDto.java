package com.example.musica_be.dto.settlement.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyRevenueDto {
  private int month;
  private Long totalRevenue;
}
