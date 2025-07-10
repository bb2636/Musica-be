package com.example.musica_be.service.payment;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureViewLog;
import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.payment.PaymentStatus;
import com.example.musica_be.dto.payment.*;
import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.lecture.LectureViewLogRepository;
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
  private final LectureRepository lectureRepository;
  private final LectureViewLogRepository lectureViewLogRepository;

  // 수강 중인 강의
  public List<EnrolledClassDto> listEnrolledClasses(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Payment> payments = paymentRepository.findAllByUserId(userId);
    List<EnrolledClassDto> enrolledClasses = new ArrayList<>();

    for (Payment payment : payments) {
      Classes clazz = payment.getClasses();
      Long classId = clazz.getId();

      // 강의 리스트 조회
      List<Lecture> lectures = lectureRepository.findByClassesId(classId);

      // 전체 강의 길이 (초 단위)
      int totalDuration = lectures.stream().mapToInt(lecture -> lecture.getProgress() != null ? lecture.getProgress() : 0).sum();

      // 유저의 시청 기록
      List<LectureViewLog> logs = lectureViewLogRepository.findByUserIdAndLecture_Classes_Id(userId, classId);

      // 총 시청 시간
      int watchedSeconds = logs.stream().mapToInt(log -> log.getDuration_seconds().toSecondOfDay()).sum();

      // 진도율 계산
      int progressPercent = totalDuration == 0 ? 0 : (int) Math.round(((double) watchedSeconds / totalDuration) * 100.0);

      // Dto 생성
      EnrolledClassDto dto = new EnrolledClassDto(payment.getId(), classId, clazz.getTitle(), clazz.getThumbnailUrl(), clazz.getInstructor().getName(), payment.getAmount(), progressPercent, payment.getPaid_at());

      enrolledClasses.add(dto);
    }

    return enrolledClasses;
  }

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

      PaymentSummaryDto dto = PaymentSummaryDto.builder().payment_id(payment.getId()).title(title).thumbnailUrl(firstClass.getThumbnailUrl()).amount(payment.getAmount()).status(String.valueOf(payment.getStatus_id())) // enum -> 문자열
          .paid_at(payment.getPaid_at()).build();

      result.add(dto);
    }

    return result;
  }

  @Transactional
  public List<PaymentGroupDto> getGroupedPayments(String jwt, Long reservationId) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Payment> payments;

    if (reservationId != null) {
      // reservationId에 해당하는 결제 단일 조회 (유저 소유 확인 포함)
      Payment payment = paymentRepository.findByIdAndUserId(reservationId, userId)
          .orElseThrow(() -> new IllegalArgumentException("해당 예약 결제 내역을 찾을 수 없습니다."));
      payments = List.of(payment);
    } else {
      // 전체 결제 조회
      payments = paymentRepository.findAllByUserId(userId);
    }

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
          .totalAmount(payment.getAmount())
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
    int totalAmount = cartItems.stream().mapToInt(item -> item.getClasses().getClassPrice() * item.getQuantity().intValue()).sum();

    // Payment 생성
    Payment payment = new Payment();
    payment.setUser(cart.getUser());
    payment.setAmount(totalAmount);
    payment.setPaid_at(LocalDateTime.now());
    payment.setStatus_id(paymentStatusRepository.findByName("PAID")
        .orElseThrow(() -> new IllegalArgumentException("PAID 상태가 존재하지 않습니다."))); // enum or 객체
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
    return PaymentResponseDto.builder().status("success").message("결제가 완료되었습니다.").build();
  }

  @Transactional
  public CancelPaymentResponseDto cancelEnrolledClasses(String jwt, CancelPaymentRequestDto request) {
    // 1. JWT → userId 추출
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    // 2. 결제 내역 조회
    Payment payment = paymentRepository.findById(request.getPayment_id()).orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다."));

    // 3. 결제 소유자 확인
    if (!payment.getUser().getId().equals(userId)) {
      throw new SecurityException("본인의 결제 내역만 취소할 수 있습니다.");
    }

    // 4. 결제일 기준 7일 이내인지 체크
    LocalDateTime paidAt = payment.getPaid_at();
    LocalDateTime now = LocalDateTime.now();
    if (paidAt.plusDays(7).isBefore(now)) {
      throw new IllegalStateException("결제일로부터 7일 이내에만 취소가 가능합니다.");
    }

    // 5. 결제 항목 조회
    List<PaymentItem> paymentItems = paymentItemRepository.findAllById(request.getPayment_item_ids());

    // 6. 모든 항목이 해당 결제에 속하는지 검증하고, 강의 시청 기록 확인 (3초 이상 시청 시 취소 불가)
    for (PaymentItem item : paymentItems) {
      if (!item.getPayment().getId().equals(payment.getId())) {
        throw new IllegalArgumentException("해당 결제의 항목이 아닙니다.");
      }

      // PaymentItem에서 관련된 Classes 정보 (결제 항목이 등록한 클래스를 의미한다고 가정)
      Classes clazz = item.getClasses();
      Long classId = clazz.getId();

      // 유저의 시청 기록 조회 (PaymentItem에 해당하는 클래스 내 강의에 대해)
      List<LectureViewLog> logs = lectureViewLogRepository.findByUserIdAndLecture_Classes_Id(userId, classId);

      // 총 시청 시간(초) 합산 (LectureViewLog.durationSeconds는 int 타입으로 가정)
      int watchedSeconds = logs.stream().mapToInt(LectureViewLog::getDurationSeconds).sum();

      if (watchedSeconds >= 3) {
        throw new IllegalStateException("3초 이상 강의를 시청하여 취소(환불)가 불가능합니다.");
      }
    }

    // 7.  상태가 "CANCELED"인 PaymentStatus 엔티티 조회
    PaymentStatus canceledStatus = paymentStatusRepository.findByName("CANCELED")
        .orElseThrow(() -> new IllegalArgumentException("CANCELED 상태가 존재하지 않습니다."));

    // 7-1. 결제 상태 업데이트
    payment.setStatus_id(canceledStatus);
    payment.setPaid_at(LocalDateTime.now());
    paymentRepository.save(payment);

    // 필요하다면 결제 항목들(PaymentItem)도 업데이트 처리합니다.

    // 8. 응답 DTO 생성 및 반환
    return CancelPaymentResponseDto.builder()
        .status("CANCELED")
        .message("결제가 성공적으로 취소되었습니다.")
        .is_cancelable(true)
        .build();
  }
}