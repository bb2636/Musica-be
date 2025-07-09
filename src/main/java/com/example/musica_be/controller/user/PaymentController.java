package com.example.musica_be.controller.user;

import com.example.musica_be.dto.payment.*;
import com.example.musica_be.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  // 수강 중인 클래스 조회
  @GetMapping("/enrolled-classes")
  public ResponseEntity<List<EnrolledClassDto>> getEnrolledClasses(
      @RequestHeader("Authorization") String jwt) {
    List<EnrolledClassDto> enrolledClasses = paymentService.getEnrolledClasses(jwt);
    return ResponseEntity.ok(enrolledClasses);
  }

  // 결제 요약 리스트 조회
  @GetMapping("/summaries")
  public ResponseEntity<List<PaymentSummaryDto>> getPaymentSummaries(
      @RequestHeader("Authorization") String jwt) {
    List<PaymentSummaryDto> summaries = paymentService.getPaymentSummaries(jwt);
    return ResponseEntity.ok(summaries);
  }

  // 결제 그룹별 상세 조회
  @GetMapping("/grouped")
  public ResponseEntity<List<PaymentGroupDto>> getGroupedPayments(
      @RequestHeader("Authorization") String jwt) {
    List<PaymentGroupDto> groupedPayments = paymentService.getGroupedPayments(jwt);
    return ResponseEntity.ok(groupedPayments);
  }

  // 결제 완료
  @PostMapping("/complete")
  public ResponseEntity<PaymentResponseDto> completePayment(
      @RequestHeader("Authorization") String jwt) {
    PaymentResponseDto response = paymentService.completePayment(jwt);
    return ResponseEntity.ok(response);
  }

  // 강의 수강 취소
  @PostMapping("/cancel")
  public ResponseEntity<CancelPaymentResponseDto> cancelEnrolledClasses(
      @RequestHeader("Authorization") String jwt,
      @RequestBody CancelPaymentRequestDto request) {
    CancelPaymentResponseDto response = paymentService.cancelEnrolledClasses(jwt, request);
    return ResponseEntity.ok(response);
  }
}
