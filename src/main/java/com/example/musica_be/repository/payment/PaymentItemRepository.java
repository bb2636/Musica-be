package com.example.musica_be.repository.payment;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.dto.classes.ClassCardStatisticsDto;
import com.example.musica_be.dto.classes.ClassesStudentCountDto;
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

    @Query("""
            SELECT new com.example.musica_be.dto.classes.ClassesStudentCountDto(p.classes.id, COUNT(p))
            FROM PaymentItem p
            WHERE p.classes.id IN :classIds
            AND p.payment.status.name <> 'CANCELED'
            GROUP BY p.classes.id
        """)
    List<ClassesStudentCountDto> getStudentCounts(@Param("classIds") List<Long> classIds);

    boolean existsByPayment_User_IdAndClasses_Id(Long userId, Long classesId);

    // 강사 총 수입
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentItem p
            JOIN p.payment pay
            JOIN pay.status s
            WHERE p.classes.instructor.id = :instructorId
              AND s.name = 'PAID'
        """)
    int sumTotalRevenueByInstructorId(@Param("instructorId") Long instructorId);

    // 강사 월 수입
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentItem p
            JOIN p.payment pay
            JOIN pay.status s
            WHERE p.classes.instructor.id = :instructorId
              AND s.name = 'PAID'
              AND FUNCTION('YEAR', pay.paidAt) = :year
              AND FUNCTION('MONTH', pay.paidAt) = :month
        """)
    int sumMonthlyRevenueByInstructorId(
        @Param("instructorId") Long instructorId,
        @Param("year") int year,
        @Param("month") int month
    );

    // ClassCardStatisticsDto용
    @Query("""
        SELECT new com.example.musica_be.dto.classes.ClassCardStatisticsDto(p.classes.id, COUNT(p), 0L, 0.0, 0L)
        FROM PaymentItem p
        WHERE p.classes.id IN :classIds
        AND p.payment.status.name <> 'CANCELED'
        GROUP BY p.classes.id
        """)
    List<ClassCardStatisticsDto> getStudentStatsForCard(@Param("classIds") List<Long> classIds);

}