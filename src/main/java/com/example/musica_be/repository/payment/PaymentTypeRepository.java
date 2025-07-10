package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Long> {
  Optional<PaymentType> findByName(String paymentType);
}
