package com.example.musica_be.service.settlement;

import com.example.musica_be.domain.settlement.InstructorAccount;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.settlement.request.AccountRequestDto;
import com.example.musica_be.dto.settlement.response.AccountResponseDto;
import com.example.musica_be.dto.settlement.response.MonthlyRevenueDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.settlement.InstructorAccountRepository;
import com.example.musica_be.repository.settlement.SettlementRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {
  private final InstructorAccountRepository instructorAccountRepository;
  private final UserRepository userRepository;
  private final PaymentItemRepository paymentItemRepository;
  private final ClassesRepository classesRepository;
  private final SettlementRepository settlementRepository;

  public AccountResponseDto settlementAdd(String jwt, AccountRequestDto accountRequestDto) {
    Long userIdFromToken = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    User instructor = userRepository.findById(userIdFromToken)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

    InstructorAccount instructorAccount = InstructorAccount.builder()
        .user(instructor)
        .bank_name(accountRequestDto.getBankName())
        .account_number(accountRequestDto.getAccountNumber())
        .account_holder_name(accountRequestDto.getAccountHolder())
        .created_at(LocalDateTime.now())
        .build();

    InstructorAccount saved = instructorAccountRepository.save(instructorAccount);

    return AccountResponseDto.builder()
        .bankName(saved.getBank_name())
        .accountNumber(saved.getAccount_number())
        .account_holder(saved.getAccount_holder_name())
        .build();
  }

  public AccountResponseDto settlementUpdate(String jwt, AccountRequestDto accountRequestDto) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    User instructor = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

    InstructorAccount instructorAccount = instructorAccountRepository.findByUser(instructor)
        .orElseThrow(() -> new IllegalStateException("정산 계좌가 등록되어 있지 않습니다."));

    // 정보 수정
    instructorAccount.setBank_name(accountRequestDto.getBankName());
    instructorAccount.setAccount_number(accountRequestDto.getAccountNumber());
    instructorAccount.setAccount_holder_name(accountRequestDto.getAccountHolder());

    InstructorAccount updated = instructorAccountRepository.save(instructorAccount);

    return AccountResponseDto.builder()
        .bankName(updated.getBank_name())
        .accountNumber(updated.getAccount_number())
        .account_holder(updated.getAccount_holder_name())
        .build();
  }

  public void settlementDelete(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    User instructor = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

    InstructorAccount instructorAccount = instructorAccountRepository.findByUser(instructor)
        .orElseThrow(() -> new IllegalStateException("정산 계좌가 등록되어 있지 않습니다."));

    instructorAccountRepository.delete(instructorAccount);
  }

  public AccountResponseDto settlementGet(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    User instructor = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

    InstructorAccount instructorAccount = instructorAccountRepository.findByUser(instructor)
        .orElseThrow(() -> new IllegalStateException("정산 계좌가 등록되어 있지 않습니다."));

    return AccountResponseDto.builder()
        .bankName(instructorAccount.getBank_name())
        .accountNumber(instructorAccount.getAccount_number())
        .account_holder(instructorAccount.getAccount_holder_name())
        .build();
  }

  public Long getTotalSalesByInstructor(String jwt) {
    Long instructorId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    return settlementRepository.sumTotalAmountByInstructorId(instructorId);
  }

  public Long getTotalSalesByClass(String jwt) {
    Long instructorId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    return settlementRepository.sumTotalAmountByClassId(instructorId);
  }

  public List<MonthlyRevenueDto> getMonthlySalesByJwtAndYear(String jwt, int year) {
    Long instructorId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    List<Object[]> results = settlementRepository.findMonthlySalesByInstructorAndYear(instructorId, year);

    List<MonthlyRevenueDto> monthlyRevenueList = new ArrayList<>();
    // 1월부터 12월까지 기본 0으로 초기화
    for (int month = 1; month <= 12; month++) {
      monthlyRevenueList.add(MonthlyRevenueDto.builder()
          .month(month)
          .totalRevenue(0L)
          .build());
    }

    // 쿼리 결과를 DTO에 반영
    for (Object[] row : results) {
      int month = ((Number) row[0]).intValue();
      Long revenue = ((Number) row[1]).longValue();
      monthlyRevenueList.set(month - 1,
          MonthlyRevenueDto.builder()
              .month(month)
              .totalRevenue(revenue)
              .build());
    }

    return monthlyRevenueList;
  }

  public List<MonthlyRevenueDto> getMonthlySalesByClassAndYear(String jwt, int year) {
    Long instructorId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Object[]> results = settlementRepository.findMonthlySalesByClassAndYear(instructorId, year);

    // 1월부터 12월까지 0원으로 초기화
    List<MonthlyRevenueDto> monthlyRevenueList = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      monthlyRevenueList.add(MonthlyRevenueDto.builder()
          .month(month)
          .totalRevenue(0L)
          .build());
    }

    // 쿼리 결과를 반영
    for (Object[] row : results) {
      int month = ((Number) row[0]).intValue();
      Long revenue = ((Number) row[1]).longValue();

      monthlyRevenueList.set(month - 1,
          MonthlyRevenueDto.builder()
              .month(month)
              .totalRevenue(revenue)
              .build());
    }

    return monthlyRevenueList;
  }
}
