package com.example.musica_be.repository.settlement;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.settlement.Settlement;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Settlement s WHERE s.user = :instructorId")
  Long sumTotalAmountByInstructorId(@Param("instructorId") Long instructorId);

  @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Settlement s WHERE s.classes.id = :classId")
  Long sumTotalAmountByClassId(@Param("classId") Long classId);

  @Query("SELECT FUNCTION('MONTH', s.settledAt) AS month, COALESCE(SUM(s.totalAmount), 0) " +
      "FROM Settlement s " +
      "WHERE s.classes.instructor.id = :instructorId " +
      "AND FUNCTION('YEAR', s.settledAt) = :year " +
      "GROUP BY FUNCTION('MONTH', s.settledAt) " +
      "ORDER BY FUNCTION('MONTH', s.settledAt)")
  List<Object[]> findMonthlySalesByInstructorAndYear(@Param("instructorId") Long instructorId,
                                                     @Param("year") int year);

  @Query(value = """
        SELECT MONTH(settled_at) AS month, COALESCE(SUM(total_amount), 0) AS total
        FROM settlement
        WHERE class_id = :classId
          AND YEAR(settled_at) = :year
        GROUP BY MONTH(settled_at)
        ORDER BY MONTH(settled_at)
        """, nativeQuery = true)
  List<Object[]> findMonthlySalesByClassAndYear(@Param("classId") Long classId,
                                                @Param("year") int year);

  boolean existsByUserAndClassesAndSettlementMonth(User user, Classes classes, String settlementMonth);
}
