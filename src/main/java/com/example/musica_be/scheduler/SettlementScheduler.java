package com.example.musica_be.scheduler;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.settlement.Settlement;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.settlement.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final PaymentItemRepository paymentItemRepository;
    private final SettlementRepository settlementRepository;

    // 매월 25일 새벽 00:010 실행
    @Scheduled(cron = "0 10 0 25 * *")
    public void generateMonthlySettlements() {
        log.info("🧾 [SettlementScheduler] Started monthly settlement generation");

        LocalDate now = LocalDate.now();
        LocalDate targetMonth = now.minusMonths(1);
        String settlementMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<PaymentItem> items = paymentItemRepository.findDonePaymentsInMonth(targetMonth.getYear(), targetMonth.getMonthValue());

        // Instructor + Class 단위로 그룹핑
        Map<String, List<PaymentItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(item -> {
                    Long instructorId = item.getClasses().getInstructor().getId();
                    Long classId = item.getClasses().getId();
                    return instructorId + "_" + classId;
                }));

        List<Settlement> settlements = new ArrayList<>();

        for (List<PaymentItem> groupItems : grouped.values()) {
            Classes clazz = groupItems.get(0).getClasses();
            User instructor = clazz.getInstructor();

            long totalAmount = groupItems.stream()
                .mapToLong(PaymentItem::getAmount)
                .sum();

            // 정산 비율: 강사 6 / 10 → 60%
            long netAmount = totalAmount * 6 / 10;
            long commissionRate = 40L; // 40% 수수료

            boolean exists = settlementRepository.existsByUserAndClassesAndSettlementMonth(instructor, clazz, settlementMonth);
            if (exists) continue;

            Settlement settlement = Settlement.builder()
                .user(instructor)
                .classes(clazz)
                .totalAmount(totalAmount)
                .commissionRate(commissionRate)
                .netAmount(netAmount)
                .settlementMonth(settlementMonth)
                .settledAt(LocalDateTime.now())
                .build();

            settlements.add(settlement);
        }

        settlementRepository.saveAll(settlements);
        log.info("✅ [SettlementScheduler] Saved settlements: {}", settlements.size());
    }
}
