package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentSummaryDto {
    private Long payment_id;
    private String title;
    private String thumbnailUrl;
    private int amount;
    private String status;
    private LocalDateTime paid_at;
}
