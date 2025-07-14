package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.classes.Classes;
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

  int countByClasses(Classes classes);

  @Query("SELECT pi FROM PaymentItem pi " +
      "WHERE MONTH(pi.payment.paidAt) = :month " +
      "AND YEAR(pi.payment.paidAt) = :year " +
      "AND pi.payment.status.name = 'DONE'")
  List<PaymentItem> findDonePaymentsInMonth(@Param("year") int year, @Param("month") int month);
}