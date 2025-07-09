package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
  List<Payment> findAllByUserId(Long userId);
}
