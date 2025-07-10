package com.example.musica_be.domain.payment;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PaymentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
}
