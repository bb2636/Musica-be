package com.example.musica_be.service.payment;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import com.example.musica_be.domain.payment.Payment;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.payment.PaymentStatus;
import com.example.musica_be.dto.payment.*;
import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import com.example.musica_be.repository.lecture.LectureProgressRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.payment.PaymentRepository;
import com.example.musica_be.repository.payment.PaymentStatusRepository;
import com.example.musica_be.repository.payment.PaymentTypeRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private final PaymentRepository paymentRepository;
  private final PaymentItemRepository paymentItemRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PaymentStatusRepository paymentStatusRepository;
  private final LectureRepository lectureRepository;
  private final LectureProgressRepository lectureProgressRepository;
  private final PaymentTypeRepository paymentTypeRepository;

  @Qualifier("tossWebClient")
  private final WebClient tossWebClient;

  // 수강 중인 강의
  public List<EnrolledClassDto> listEnrolledClasses(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<PaymentItem> paymentItems = paymentItemRepository.findByUserId(userId);
    List<EnrolledClassDto> enrolledClasses = new ArrayList<>();

    for (PaymentItem paymentItem : paymentItems) {
      Payment payment = paymentItem.getPayment();

      // 🔒 CANCELED 상태의 결제는 제외
      if ("CANCELED".equalsIgnoreCase(payment.getStatus().getName())) {
        continue;
      }

      Long classId = paymentItem.getClasses().getId();
      Classes clazz = paymentItem.getClasses();

      // 강의 리스트 조회
      List<Lecture> lectures = lectureRepository.findByClassesId(classId);

      // 전체 강의 길이 (초 단위)
      int totalDuration = lectures.stream()
          .mapToInt(lecture -> lecture.getDuration() != null ? lecture.getDuration() : 0)
          .sum();

      // 유저의 시청 기록
      List<LectureProgress> logs = lectureProgressRepository.findByUserIdAndLecture_Classes_Id(userId, classId);
      // 총 시청 시간
      int watchedSeconds = logs.stream()
          .mapToInt(LectureProgress::getWatchedSeconds)
          .sum();

      // 진도율 계산
      int progressPercent = totalDuration == 0 ? 0 : (int) Math.round(((double) watchedSeconds / totalDuration) * 100.0);

      // Dto 생성
      EnrolledClassDto dto = new EnrolledClassDto(
          paymentItem.getId(),
          classId,
          clazz.getTitle(),
          clazz.getThumbnailUrl(),
          clazz.getInstructor().getName(),
          payment.getAmount(),
          progressPercent,
          payment.getPaidAt()
      );

      enrolledClasses.add(dto);
    }

    return enrolledClasses;
  }


  @Transactional
  public List<PaymentSummaryDto> getPaymentSummaries(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    List<Payment> payments = paymentRepository.findAllByUserId(userId);

    // paidAt 내림차순 정렬 (최근 → 과거)
    payments.sort(Comparator.comparing(Payment::getPaidAt).reversed());

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
          .paymentId(payment.getId())
          .title(title)
          .thumbnailUrl(firstClass.getThumbnailUrl())
          .amount(payment.getAmount())
          .status(String.valueOf(payment.getStatus())) // enum → 문자열
          .paidAt(payment.getPaidAt())
          .build();

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
            .paymentItemId(item.getId())
            .classId(classes.getId())
            .title(classes.getTitle())
            .status(item.getPaymentStatus().getName())
            .thumbnailUrl(classes.getThumbnailUrl())
            .instructorName(classes.getInstructor().getName())
            .amount(classes.getClassPrice())
            .build();
      }).toList();

      PaymentGroupDto groupDto = PaymentGroupDto.builder()
          .paymentId(payment.getId())
          .totalAmount(payment.getAmount())
          .paidAt(payment.getPaidAt())
          .paymentItems(classList)
          .status(payment.getStatus().getName())
          .build();

      result.add(groupDto);
    }
    log.info(result.toString());
    return result;
  }


  @Transactional
  public PaymentResponseDto tossCompletePaymentByCartItemIds(String paymentKey, String orderId, int amount, List<Long> cartItemIds) {
    List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
    log.info("카트아이템 크기 "+cartItems.size());
    if (cartItems.isEmpty()) {
      throw new IllegalStateException("선택한 장바구니 항목이 존재하지 않습니다.");
    }

    for (CartItem item : cartItems) {
      System.out.println("클래스 제목: " + item.getClasses().getTitle());
      System.out.println("클래스 가격: " + item.getClasses().getClassPrice());
      System.out.println("수량: " + item.getQuantity());
    }

    // 카트 검증: 모든 아이템이 동일한 Cart를 참조하는지 확인
    Cart cart = cartItems.get(0).getCart();
    boolean sameCart = cartItems.stream().allMatch(item -> item.getCart().getId().equals(cart.getId()));
    if (!sameCart) {
      throw new IllegalArgumentException("선택한 장바구니 항목들이 서로 다른 장바구니에 속해 있습니다.");
    }

    // 총 결제 금액 계산
    int totalAmount = cartItems.stream()
        .mapToInt(item -> item.getClasses().getClassPrice())
        .sum();

    log.info(String.valueOf(totalAmount));

    if (totalAmount != amount) {
      throw new IllegalArgumentException("금액 불일치");
    }

    // Toss 결제 승인 요청
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("paymentKey", paymentKey);
    requestBody.put("orderId", orderId);
    requestBody.put("amount", amount);

    PaymentConfirmResponse dto;
    try {
      dto = tossWebClient.post()
          .uri("/payments/confirm")
          .bodyValue(requestBody)
          .retrieve()
          .bodyToMono(PaymentConfirmResponse.class)
          .block();
    } catch (WebClientResponseException e) {
      System.err.println("Toss 결제 승인 실패: " + e.getResponseBodyAsString());
      throw new RuntimeException("결제 승인 실패: " + e.getMessage());
    }

    if (dto == null) {
      throw new IllegalStateException("결제 응답이 null입니다.");
    }

    if ("DONE".equals(dto.getStatus()) || "카드".equals(dto.getMethod())) {
      dto.setStatus("PAID");
      dto.setMethod("CARD");
    }

    // Payment 생성
    Payment payment = new Payment();
    payment.setOrderId(dto.getOrderId());
    payment.setPaymentKey(dto.getPaymentKey());
    payment.setUser(cart.getUser());
    payment.setAmount(totalAmount);
    payment.setPaidAt(LocalDateTime.now());
    payment.setPayType(paymentTypeRepository.findByName(dto.getMethod())
        .orElseThrow(() -> new IllegalArgumentException(dto.getMethod() + " 결제 수단이 존재하지 않습니다.")));
    payment.setStatus(paymentStatusRepository.findByName("PAID")
        .orElseThrow(() -> new IllegalArgumentException("PAID 상태가 존재하지 않습니다.")));

    paymentRepository.save(payment);

    // PaymentItem 생성
    for (CartItem item : cartItems) {
      PaymentItem paymentItem = new PaymentItem();
      paymentItem.setPayment(payment);
      paymentItem.setClasses(item.getClasses());
      paymentItem.setQuantity(item.getQuantity());
      paymentItem.setAmount(item.getAmount());
      paymentItem.setPaymentStatus(paymentStatusRepository.findByName("PAID").orElseThrow());
      paymentItemRepository.save(paymentItem);
    }

    // 선택된 CartItem만 삭제
    cartItemRepository.deleteAll(cartItems);

    // 응답 반환
    return PaymentResponseDto.builder()
        .status("success")
        .message("선택한 항목의 결제가 완료되었습니다.")
        .build();
  }

  @Transactional
  public CancelPaymentResponseDto tossCancelPayment(CancelPaymentRequestDto request, String jwt) {
    // 1. JWT → userId 추출
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));

    // 2. 결제 내역 조회
    Payment payment = paymentRepository.findById(request.getPayment_id())
        .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다."));

    // 3. 결제 소유자 확인
    if (!payment.getUser().getId().equals(userId)) {
      throw new SecurityException("본인의 결제 내역만 취소할 수 있습니다.");
    }

    // 4. 결제일 기준 7일 이내인지 체크
    if (payment.getPaidAt().plusDays(7).isBefore(LocalDateTime.now())) {
      throw new IllegalStateException("결제일로부터 7일 이내에만 취소가 가능합니다.");
    }

    // 5. 요청된 결제 항목 가져오기
    List<PaymentItem> requestItems = paymentItemRepository.findAllById(request.getPayment_item_ids());

    // 6. "CANCELED" 상태 가져오기
    PaymentStatus canceledItemStatus = paymentStatusRepository.findByName("CANCELED")
        .orElseThrow(() -> new IllegalArgumentException("CANCELED 상태가 존재하지 않습니다."));

    int cancelAmount = 0;

    // 7. 결제 항목 검증 및 상태 변경
    for (PaymentItem item : requestItems) {
      if (!item.getPayment().getId().equals(payment.getId())) {
        throw new IllegalArgumentException("해당 결제의 항목이 아닙니다.");
      }

      if (item.getPaymentStatus().getName().equals("CANCELED")) {
        throw new IllegalStateException("이미 취소된 항목입니다.");
      }

      // 강의 시청 여부 체크 (3초 이상 시청 시 환불 불가)
      List<LectureProgress> logs = lectureProgressRepository.findByUserIdAndLecture_Classes_Id(userId, item.getClasses().getId());
      int watchedSeconds = logs.stream().mapToInt(LectureProgress::getWatchedSeconds).sum();

      if (watchedSeconds >= 3) {
        throw new IllegalStateException("3초 이상 강의를 시청하여 취소(환불)가 불가능합니다.");
      }

      // 환불 가능한 항목의 금액 누적
      cancelAmount += item.getAmount();

      // 항목 상태 업데이트
      item.setPaymentStatus(canceledItemStatus);
      paymentItemRepository.save(item);
    }

    // 8. Toss API에 부분 환불 요청
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("cancelReason", "사용자 환불 요청");
      requestBody.put("cancelAmount", cancelAmount);

      PaymentCancelResponseDto responseDto = tossWebClient.post()
          .uri("/payments/{paymentKey}/cancel", payment.getPaymentKey())
          .bodyValue(requestBody)
          .retrieve()
          .bodyToMono(PaymentCancelResponseDto.class)
          .block();

      if (responseDto == null) {
        throw new RuntimeException("결제 취소 응답이 없습니다.");
      }

    } catch (WebClientResponseException e) {
      System.err.println("Toss 결제 취소 실패: " + e.getResponseBodyAsString());
      throw new RuntimeException("결제 취소 실패: " + e.getMessage(), e);
    }

    // 9. 전체 항목 상태에 따라 결제 상태 업데이트
    List<PaymentItem> allItems = paymentItemRepository.findByPaymentId(payment.getId());
    boolean allCanceled = allItems.stream().allMatch(i -> i.getPaymentStatus().getName().equals("CANCELED"));
    boolean anyCanceled = allItems.stream().anyMatch(i -> i.getPaymentStatus().getName().equals("CANCELED"));

    if (allCanceled) {
      PaymentStatus canceled = paymentStatusRepository.findByName("CANCELED")
          .orElseThrow(() -> new IllegalArgumentException("CANCELED 상태 없음"));
      payment.setStatus(canceled);
    } else if (anyCanceled) {
      PaymentStatus partialCanceled = paymentStatusRepository.findByName("PARTIALLY_CANCELED")
          .orElseThrow(() -> new IllegalArgumentException("PARTIALLY_CANCELED 상태 없음"));
      payment.setStatus(partialCanceled);
    }

    // 10. 잔여 결제 금액 업데이트
    int updatedAmount = allItems.stream()
        .filter(i -> !i.getPaymentStatus().getName().equals("CANCELED"))
        .mapToInt(PaymentItem::getAmount)
        .sum();
    payment.setAmount(updatedAmount);
    paymentRepository.save(payment);

    // 11. 응답 반환
    return CancelPaymentResponseDto.builder()
        .status(payment.getStatus().getName())
        .message("선택한 결제 항목이 성공적으로 취소되었습니다.")
        .is_cancelable(true)
        .build();
  }
}

// cancel 바닐라
//@Transactional
//  public CancelPaymentResponseDto cancelEnrolledClasses(String jwt, CancelPaymentRequestDto request) {
//    // 1. JWT → userId 추출
//    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
//
//    // 2. 결제 내역 조회
//    Payment payment = paymentRepository.findById(request.getPayment_id())
//        .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다."));
//
//    // 3. 결제 소유자 확인
//    if (!payment.getUser().getId().equals(userId)) {
//      throw new SecurityException("본인의 결제 내역만 취소할 수 있습니다.");
//    }
//
//    // 4. 결제일 기준 7일 이내인지 체크
//    LocalDateTime paidAt = payment.getPaid_at();
//    if (paidAt.plusDays(7).isBefore(LocalDateTime.now())) {
//      throw new IllegalStateException("결제일로부터 7일 이내에만 취소가 가능합니다.");
//    }
//
//    // 5. 요청된 결제 항목 가져오기
//    List<PaymentItem> requestItems = paymentItemRepository.findAllById(request.getPayment_item_ids());
//
//    // 6. "CANCELED" 상태 가져오기
//    PaymentStatus canceledItemStatus = paymentStatusRepository.findByName("CANCELED")
//        .orElseThrow(() -> new IllegalArgumentException("CANCELED 상태가 존재하지 않습니다."));
//
//    // 7. 결제 항목 검증 및 상태 변경
//    for (PaymentItem item : requestItems) {
//      if (!item.getPayment().getId().equals(payment.getId())) {
//        throw new IllegalArgumentException("해당 결제의 항목이 아닙니다.");
//      }
//
//      if (item.getPaymentStatus().getName().equals("CANCELED")) {
//        throw new IllegalStateException("이미 취소된 항목입니다.");
//      }
//
//      Classes clazz = item.getClasses();
//      List<LectureViewLog> logs = lectureViewLogRepository.findByUserIdAndLecture_Classes_Id(userId, clazz.getId());
//      int watchedSeconds = logs.stream().mapToInt(LectureViewLog::getDurationSeconds).sum();
//
//      if (watchedSeconds >= 3) {
//        throw new IllegalStateException("3초 이상 강의를 시청하여 취소(환불)가 불가능합니다.");
//      }
//
//      // 항목 상태를 CANCELED로 설정
//      item.setPaymentStatus(canceledItemStatus);
//      paymentItemRepository.save(item);
//    }
//
//    // 8. 전체 결제 항목을 가져와서 결제 상태 업데이트
//    List<PaymentItem> allItems = paymentItemRepository.findByPaymentId(payment.getId());
//    boolean allCanceled = allItems.stream().allMatch(i -> i.getPaymentStatus().getName().equals("CANCELED"));
//    boolean anyCanceled = allItems.stream().anyMatch(i -> i.getPaymentStatus().getName().equals("CANCELED"));
//
//    if (allCanceled) {
//      PaymentStatus canceled = paymentStatusRepository.findByName("CANCELED")
//          .orElseThrow(() -> new IllegalArgumentException("CANCELED 상태 없음"));
//      payment.setStatus(canceled);
//    } else if (anyCanceled) {
//      PaymentStatus partialCanceled = paymentStatusRepository.findByName("PARTIALLY_CANCELED")
//          .orElseThrow(() -> new IllegalArgumentException("PARTIALLY_CANCELED 상태 없음"));
//      payment.setStatus(partialCanceled);
//    }
//    int updatedAmount = allItems.stream()
//        .filter(i -> !i.getPaymentStatus().getName().equals("CANCELED"))
//        .mapToInt(PaymentItem::getAmount)
//        .sum();
//
//    payment.setAmount(updatedAmount);
//    paymentRepository.save(payment);
//
//    // 9. 응답 반환
//    return CancelPaymentResponseDto.builder()
//        .status(payment.getStatus().getName())
//        .message("선택한 결제 항목이 성공적으로 취소되었습니다.")
//        .is_cancelable(true)
//        .build();
//  }

// completePayment 바닐라
//  @Transactional
//  public PaymentResponseDto completePayment(String jwt, PaymentStatusUpdateRequestDto dto) {
//    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
//    Cart cart = cartRepository.findByUserId(userId);
//    if (cart == null) throw new IllegalStateException("장바구니가 존재하지 않습니다.");
//
//    List<CartItem> cartItems = cartItemRepository.findByCart(cart);
//    if (cartItems.isEmpty()) throw new IllegalStateException("장바구니가 비어 있습니다.");
//
//    // 총 결제 금액 계산
//    int totalAmount = cartItems.stream().mapToInt(item -> item.getClasses().getClassPrice()).sum();
//
//    if (totalAmount != dto.getAmount()) {
//      throw new IllegalArgumentException("금액 불일치");
//    }
//
//    // Payment 생성
//    Payment payment = new Payment();
//    payment.setOrderId(dto.getOrderId());
//    payment.setPaymentKey(dto.getPaymentKey());
//    payment.setUser(cart.getUser());
//    payment.setAmount(totalAmount);
//    payment.setPaid_at(LocalDateTime.now());
//    payment.setPayType(paymentTypeRepository.findByName(dto.getPay_method())
//        .orElseThrow(() -> new IllegalArgumentException(dto.getPay_method() + " 상태가 존재하지 않습니다.")));
//    payment.setStatus(paymentStatusRepository.findByName("PAID")
//        .orElseThrow(() -> new IllegalArgumentException("PAID 상태가 존재하지 않습니다."))); // enum or 객체
//    paymentRepository.save(payment);
//
//    // PaymentItem 생성
//    for (CartItem item : cartItems) {
//      PaymentItem paymentItem = new PaymentItem();
//      paymentItem.setPayment(payment);
//      paymentItem.setClasses(item.getClasses());
//      paymentItem.setQuantity(item.getQuantity());
//      paymentItem.setAmount(item.getAmount());
//      paymentItem.setPaymentStatus(paymentStatusRepository.findByName("PAID").orElseThrow());
//      paymentItemRepository.save(paymentItem);
//    }
//
//    // Cart, CartItem 삭제
//    cartItemRepository.deleteAll(cartItems);
//    cartRepository.delete(cart);
//
//    // 응답 반환
//    return PaymentResponseDto.builder().status("success").message("결제가 완료되었습니다.").build();
//  }