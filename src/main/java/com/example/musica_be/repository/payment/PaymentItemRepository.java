package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
  List<PaymentItem> findByPayment(Payment payment);
  @Query("SELECT pi FROM PaymentItem pi WHERE pi.payment.user.id = :userId")
  List<PaymentItem> findByUserId(@Param("userId") Long userId);

  List<PaymentItem> findByPaymentId(Long id);

  @Query(value = "SELECT pi.* FROM payment_item pi " +
      "JOIN payment p ON pi.payment_id = p.id " +
      "JOIN payment_status ps ON p.status_id = ps.id " +
      "WHERE ps.name = 'DONE' " +
      "AND YEAR(p.paid_at) = :year " +
      "AND MONTH(p.paid_at) = :month",
      nativeQuery = true)
  List<PaymentItem> findDonePaymentsInMonth(@Param("year") int year, @Param("month") int month);
}