package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
  List<Payment> findAllByUserId(Long userId);
  Optional<Payment> findByIdAndUserId(Long reservationId, Long userId);
}