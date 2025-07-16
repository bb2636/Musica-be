package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrolledClassItemDto {
  private Long paymentItemId;
  private String status;
  private Long classId;
  private String title;
  private String thumbnailUrl;
  private String instructorName;
  private int amount;
}
