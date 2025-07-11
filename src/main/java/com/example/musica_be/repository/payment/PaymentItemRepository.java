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

  @Query("SELECT SUM(pi.amount) " +
      "FROM PaymentItem pi " +
      "JOIN pi.payment r " +                  // 클래스 수강 정보
      "JOIN pi.classes c " +                   // 클래스 엔티티
      "WHERE c.instructor.id = :instructorId")
  Long sumTotalRevenueByInstructor(@Param("instructorId") Long instructorId);

  @Query("SELECT SUM(pi.amount) " +
      "FROM PaymentItem pi " +
      "JOIN pi.payment p " +
      "JOIN pi.classes c " +
      "WHERE c.id = :classId")
  Long sumRevenueByClassId(@Param("classId") Long classId);

  @Query(value = "SELECT MONTH(p.paid_at) AS month, " +
      "SUM(pi.amount) AS totalRevenue " +
      "FROM payment_item pi " +
      "JOIN payment p ON pi.payment_id = p.id " +
      "JOIN classes c ON pi.class_id = c.id " +
      "WHERE c.instructor_id = :instructorId " +
      "  AND YEAR(p.paid_at) = :year " +
      "GROUP BY month " +
      "ORDER BY month ASC",
      nativeQuery = true)
  List<Object[]> findMonthlyRevenueByInstructorAndYear(@Param("instructorId") Long instructorId,
                                                       @Param("year") int year);

  @Query(value = "SELECT MONTH(p.paid_at) AS month, " +
      "SUM(pi.amount) AS totalRevenue " +
      "FROM payment_item pi " +
      "JOIN payment p ON pi.payment_id = p.id " +
      "JOIN classes c ON pi.class_id = c.id " +
      "WHERE c.id = :classId " +
      "  AND YEAR(p.paid_at) = :year " +
      "GROUP BY month " +
      "ORDER BY month ASC",
      nativeQuery = true)
  List<Object[]> findMonthlyRevenueByClassAndYear(@Param("classId") Long classId,
                                                  @Param("year") int year);
}