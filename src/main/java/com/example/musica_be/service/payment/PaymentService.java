package com.example.musica_be.service.payment;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.payment.PaymentStatus;
import com.example.musica_be.dto.payment.*;
import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.payment.PaymentRepository;
import com.example.musica_be.repository.payment.PaymentStatusRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final PaymentItemRepository paymentItemRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PaymentStatusRepository paymentStatusRepository;

//  수강 중인 강의
//  public List<EnrolledClassDto> getEnrolledClassesWithProgress(Long userId) {
//    List<PaymentItem> paymentItems = paymentItemRepository.findAllByPaymentUserId(userId);
//    List<EnrolledClassDto> dtos = new ArrayList<>();
//
//    for (PaymentItem pi : paymentItems) {
//      var payment = pi.getPayment();
//      var clazz = pi.getPayment().getClasses();
//      var instructor = clazz.getInstructor();
//
//      List<Lecture> lectures = clazz.getLectures();
//      int avgProgress = 0;
//      if (!lectures.isEmpty()) {
//        avgProgress = (int) lectures.stream()
//                .mapToInt(Lecture::getProgress)
//                .average()
//                .orElse(0);
//      }
//
//      EnrolledClassDto dto = new EnrolledClassDto(
//              payment.getId(),
//              clazz.getId(),
//              clazz.getTitle(),
//              clazz.getThumbnailUrl(),
//              instructor.getName(),
//              pi.getAmount(),
//              avgProgress,
//              payment.getPaidAt()
//      );
//
//      dtos.add(dto);
//    }
//
//    return dtos;
//  }

  @Transactional
  public List<PaymentSummaryDto> getPaymentSummaries(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Payment> payments = paymentRepository.findAllByUserId(userId);

    List<PaymentSummaryDto> result = new ArrayList<>();

    for (Payment payment : payments) {
      List<PaymentItem> items = paymentItemRepository.findByPayment(payment);

      if (items.isEmpty()) continue;

      Classes firstClass = items.get(0).getClasses();

      String title = firstClass.getTitle();
      if (items.size() > 1) {
        title += " 외 " + (items.size() - 1);
      }

      PaymentSummaryDto dto = PaymentSummaryDto.builder()
          .payment_id(payment.getId())
          .title(title)
          .thumbnailUrl(firstClass.getThumbnailUrl())
          .amount(payment.getAmount())
          .status(String.valueOf(payment.getStatus_id())) // enum -> 문자열
          .paid_at(payment.getPaid_at())
          .build();

      result.add(dto);
    }

    return result;
  }

  @Transactional
  public List<PaymentGroupDto> getGroupedPayments(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Payment> payments = paymentRepository.findAllByUserId(userId);

    List<PaymentGroupDto> result = new ArrayList<>();

    for (Payment payment : payments) {
      List<PaymentItem> items = paymentItemRepository.findByPayment(payment);

      if (items.isEmpty()) continue;

      List<EnrolledClassItemDto> classList = items.stream().map(item -> {
        Classes classes = item.getClasses();
        return EnrolledClassItemDto.builder()
            .payment_item_id(item.getId())
            .class_id(classes.getId())
            .title(classes.getTitle())
            .thumbnailUrl(classes.getThumbnailUrl())
            .instructorName(classes.getInstructor().getName())
            .amount(classes.getClassPrice())
            .build();
      }).toList();

      PaymentGroupDto groupDto = PaymentGroupDto.builder()
          .payment_id(payment.getId())
          .totalAmount(payment.getAmount()) // 전체 금액은 Payment에 있음
          .paid_at(payment.getPaid_at())
          .classes(classList)
          .build();

      result.add(groupDto);
    }
    return result;
  }

  @Transactional
  public PaymentResponseDto completePayment(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) throw new IllegalStateException("장바구니가 존재하지 않습니다.");

    List<CartItem> cartItems = cartItemRepository.findByCart(cart);
    if (cartItems.isEmpty()) throw new IllegalStateException("장바구니가 비어 있습니다.");

    // 총 결제 금액 계산
    int totalAmount = cartItems.stream()
        .mapToInt(item -> item.getClasses().getClassPrice() * item.getQuantity().intValue())
        .sum();

    // Payment 생성
    Payment payment = new Payment();
    payment.setUser(cart.getUser());
    payment.setAmount(totalAmount);
    payment.setPaid_at(LocalDateTime.now());
    payment.setStatus_id(paymentStatusRepository.findByName("PAID")); // enum or 객체
    paymentRepository.save(payment);

    // PaymentItem 생성
    for (CartItem item : cartItems) {
      PaymentItem paymentItem = new PaymentItem();
      paymentItem.setPayment(payment);
      paymentItem.setClasses(item.getClasses());
      paymentItem.setQuantity(item.getQuantity().intValue());
      paymentItemRepository.save(paymentItem);
    }

    // Cart, CartItem 삭제
    cartItemRepository.deleteAll(cartItems);
    cartRepository.delete(cart);

    // 응답 반환
    return PaymentResponseDto.builder()
        .status("success")
        .message("결제가 완료되었습니다.")
        .build();
  }

  @Transactional
  public CancelPaymentResponseDto cancelEnrolledClasses(String jwt, CancelPaymentRequestDto request) {
    // 1. JWT → userId 추출
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    // 2. 결제 내역 조회
    Payment payment = paymentRepository.findById(request.getPayment_id())
        .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다."));

    // 3. 결제 소유자 확인
    if (!payment.getUser().getId().equals(userId)) {
      throw new SecurityException("본인의 결제 내역만 취소할 수 있습니다.");
    }

    // 4. 결제 항목 조회
    List<PaymentItem> paymentItems = paymentItemRepository.findAllById(request.getPayment_item_ids());

    // 5. 모든 항목이 해당 결제에 속하는지 검증
    for (PaymentItem item : paymentItems) {
      if (!item.getPayment().getId().equals(payment.getId())) {
        throw new IllegalArgumentException("해당 결제의 항목이 아닙니다.");
      }
    }

    // 6. 취소 가능 여부 확인
    for (PaymentItem item : paymentItems) {
      if (!isCancelable(item)) {
        return CancelPaymentResponseDto.builder()
            .status("fail")
            .message("이미 수강 완료된 강의는 취소할 수 없습니다.")
            .is_cancelable(false)
            .build();
      }
    }

    // 7. 상태 변경
    PaymentStatus cancelledStatus = paymentStatusRepository.findByName("CANCELLED");
    for (PaymentItem item : paymentItems) {
      item.getPayment().setStatus_id(cancelledStatus);
      // 또는 item 자체에 status 필드가 있다면 item.setStatus(...)
    }

    return CancelPaymentResponseDto.builder()
        .status("success")
        .message("강의 수강이 취소되었습니다.")
        .is_cancelable(true)
        .build();

  }

  private boolean isCancelable(PaymentItem item) {
    // 결제 상태가 PAID일 때만 취소 가능하다고 가정
    String statusName = item.getPayment().getStatus_id().getName();
    if (!"PAID".equals(statusName)) {
      return false; // 결제가 완료되지 않았거나 이미 취소된 상태라면 취소 불가
    }

    // 추가로 수강 완료 여부 체크 (예: 클래스 수강 완료 플래그, 종료일 등)
    // if (item.isCompleted()) return false;

    return true;
  }
}


