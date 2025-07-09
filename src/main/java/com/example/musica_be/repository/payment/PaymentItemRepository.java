package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.dto.payment.EnrolledClassDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
  @Query("SELECT new com.example.musica_be.dto.payment.EnrolledClassDto(" +
      "pi.payment.id, c.id, c.title, c.thumbnailUrl, i.name, c.classPrice, pi.payment.paid_at) " +
      "FROM PaymentItem pi " +
      "JOIN pi.classes c " +
      "JOIN c.instructor i " +
      "WHERE pi.payment.user.id = :userId")
  List<EnrolledClassDto> findEnrolledClassesByUserId(@Param("userId") Long userId);

  List<PaymentItem> findByPayment(Payment payment);
}
