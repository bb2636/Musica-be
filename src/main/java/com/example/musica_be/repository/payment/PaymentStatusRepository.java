package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.PaymentStatus;
import org.springframework.data.repository.CrudRepository;

public interface PaymentStatusRepository extends CrudRepository<PaymentStatus, Long> {
  PaymentStatus findByName(String paid);
}
