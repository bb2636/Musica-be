package com.example.musica_be.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentItemDto {
    private Long class_id;
    private String title;
    private String thumbnailUrl;
    private String instructorName;
    private int amount;
}
