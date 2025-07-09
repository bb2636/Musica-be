package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.dto.payment.EnrolledClassDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
//  @EntityGraph(attributePaths = {
//          "paymentItems",
//          "paymentItems.reservation",
//          "paymentItems.reservation.clazz",
//          "paymentItems.reservation.clazz.instructor"
//  })
//  List<Payment> findAllByUserId(Long userId);
  List<PaymentItem> findByPayment(Payment payment);
}
