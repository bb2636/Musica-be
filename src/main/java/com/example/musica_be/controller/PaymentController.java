package com.example.musica_be.controller;

import com.example.musica_be.dto.payment.*;
import com.example.musica_be.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  // ✅ 수강 중인 클래스 목록 조회
  @GetMapping("/users/me/classes")
  public ResponseEntity<List<EnrolledClassDto>> listEnrolledClasses(
      @RequestHeader("Authorization") String jwt) {
    List<EnrolledClassDto> result = paymentService.listEnrolledClasses(jwt);
    return ResponseEntity.ok(result);
  }

 // 결제 내역 조회
  @GetMapping("/users/me/payments")
  public ResponseEntity<?> getGroupedPayments(
      @RequestHeader("Authorization") String jwt,
      @RequestParam(value = "payment_id", required = false) Long reservationId) {

    if (reservationId != null) {
      List<PaymentGroupDto> groupResult = paymentService.getGroupedPayments(jwt, reservationId);
      return ResponseEntity.ok(groupResult);
    } else {
      List<PaymentSummaryDto> summaryResult = paymentService.getPaymentSummaries(jwt);
      return ResponseEntity.ok(summaryResult);
    }
  }

  // ✅ 결제 취소 처리
  @PostMapping("/payment/cancel")
  public ResponseEntity<CancelPaymentResponseDto> cancelPayment(
      @RequestHeader("Authorization") String jwt,
      @RequestBody CancelPaymentRequestDto request) {
    CancelPaymentResponseDto result = paymentService.tossCancelPayment(request, jwt);
    return ResponseEntity.ok(result);
  }

  // 결제 완료 처리 (장바구니 결제)
  @PostMapping("/payment/cart/checkout")
  public ResponseEntity<PaymentResponseDto> completePayment(
      @RequestParam("paymentKey") String paymentKey,
      @RequestParam("orderId")String orderId,
      @RequestParam("amount")int amount,
      @RequestParam("cartId") Long cartId) {
    PaymentResponseDto response = paymentService.tossCompletePayment(paymentKey,orderId,amount,cartId);
    return ResponseEntity.ok(response);
  }
}
