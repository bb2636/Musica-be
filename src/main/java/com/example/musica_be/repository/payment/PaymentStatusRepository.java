package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.PaymentStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentStatusRepository extends CrudRepository<PaymentStatus, Long> {
  Optional<PaymentStatus> findByName(String name);
}