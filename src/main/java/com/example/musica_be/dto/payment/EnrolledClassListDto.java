package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EnrolledClassListDto {
  private Long payment_id;
  private Long class_id;
  private String title;
  private String thumbnailUrl;
  private String instructorName;
  private int amount;
  private LocalDate paid_at;
}
