package com.example.musica_be.controller;

import com.example.musica_be.dto.settlement.request.AccountRequestDto;
import com.example.musica_be.dto.settlement.response.AccountResponseDto;
import com.example.musica_be.dto.settlement.response.MonthlyRevenueDto;
import com.example.musica_be.service.settlement.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructors")
public class SettlementController {

  private final SettlementService settlementService;

  // ✅ 정산 계좌 등록
  @PostMapping("/me/account")
  public ResponseEntity<AccountResponseDto> addAccount(
      @RequestHeader("Authorization") String jwt,
      @RequestBody AccountRequestDto dto) {
    return ResponseEntity.ok(settlementService.settlementAdd(jwt, dto));
  }

  // ✅ 정산 계좌 조회
  @GetMapping("/me/account")
  public ResponseEntity<AccountResponseDto> getAccount(
      @RequestHeader("Authorization") String jwt) {
    return ResponseEntity.ok(settlementService.settlementGet(jwt));
  }

  // ✅ 정산 계좌 수정
  @PutMapping("/me/account")
  public ResponseEntity<AccountResponseDto> updateAccount(
      @RequestHeader("Authorization") String jwt,
      @RequestBody AccountRequestDto dto) {
    return ResponseEntity.ok(settlementService.settlementUpdate(jwt, dto));
  }

  // ✅ 정산 계좌 삭제
  @DeleteMapping("/me/account")
  public ResponseEntity<Void> deleteAccount(
      @RequestHeader("Authorization") String jwt) {
    settlementService.settlementDelete(jwt);
    return ResponseEntity.noContent().build();
  }

  // ✅ 강사의 전체 매출 총합
  @GetMapping("/instructors/statistics/revenue")
  public ResponseEntity<Long> getTotalSalesByInstructor(
      @RequestHeader("Authorization") String jwt) {
    return ResponseEntity.ok(settlementService.getTotalSalesByInstructor(jwt));
  }

  // ✅ 강사의 클래스별 전체 매출 총합
  @GetMapping("/classes/statistics/revenue")
  public ResponseEntity<Long> getTotalSalesByClass(
      @RequestHeader("Authorization") String jwt) {
    return ResponseEntity.ok(settlementService.getTotalSalesByClass(jwt));
  }

  // ✅ 강사의 월별 매출 (년 기준)
  @GetMapping("/statistics/year/revenue")
  public ResponseEntity<List<MonthlyRevenueDto>> getMonthlySalesByInstructor(
      @RequestHeader("Authorization") String jwt,
      @RequestParam int year) {
    return ResponseEntity.ok(settlementService.getMonthlySalesByJwtAndYear(jwt, year));
  }

  // ✅ 강사의 클래스별 월별 매출 (년 기준)
  @GetMapping("/classes/statistics/year/revenue")
  public ResponseEntity<List<MonthlyRevenueDto>> getMonthlySalesByClass(
      @RequestHeader("Authorization") String jwt,
      @RequestParam int year) {
    return ResponseEntity.ok(settlementService.getMonthlySalesByClassAndYear(jwt, year));
  }
}
