package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrolledClassDto {
  private Long payment_id;
  private Long class_id;
  private String title;
  private String thumbnailUrl;
  private String instructorName;
  private int amount;
  private int progress;
  private LocalDateTime paid_at;

  public EnrolledClassDto(Long payment_id, Long class_id, String title, String thumbnailUrl, String instructorName, int amount,int progress, LocalDateTime paid_at) {
    this.payment_id = payment_id;
    this.class_id = class_id;
    this.title = title;
    this.thumbnailUrl = thumbnailUrl;
    this.instructorName = instructorName;
    this.amount = amount;
    this.paid_at = paid_at;
    this.progress = progress;
  }
}
