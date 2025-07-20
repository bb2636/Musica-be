package com.example.musica_be.service.cart;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.payment.PaymentItem;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.cart.CartDto;
import com.example.musica_be.dto.cart.CartItemDto;
import com.example.musica_be.dto.cart.CartItemIdsDto;
import com.example.musica_be.dto.cart.CartResponseDto;
import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final UserRepository userRepository;
  private final ClassesRepository classesRepository;
  private final PaymentItemRepository paymentItemRepository;

  /**
   * 장바구니가 없으면 새로 생성해서 반환하는 공통 메서드
   */
  private Cart getOrCreateCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId);

    // 장바구니가 없으면 새로 생성
    if (cart == null) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));
      cart = new Cart();
      cart.setUser(user);
      cart.setCreated_at(LocalDateTime.now());
      cartRepository.save(cart);
    }

    return cart;
  }

  /**
   * 현재 로그인한 사용자의 장바구니 정보를 조회하고 없으면 생성함
   * 장바구니 아이템 목록, 총 가격, 총 개수 등을 반환
   */
  @Transactional
  public CartDto getCartItemList(Long userId) {
    Cart cart = getOrCreateCart(userId);

    // 장바구니 아이템 가져오기
    List<CartItem> cartItems = cartItemRepository.findAllByCartIdWithClasses(cart.getId());
    List<CartItemDto> cartItemDtoList = new ArrayList<>();
    int totalPrice = 0;

    for (CartItem cartItem : cartItems) {
      Classes classes = cartItem.getClasses();
      CartItemDto dto = CartItemDto.fromClasses(classes);
      dto.setCartItemId(cartItem.getId());
      totalPrice += classes.getClassPrice();
      cartItemDtoList.add(dto);
    }

    return CartDto.builder()
            .CartId(cart.getId())
            .userId(userId)
            .totalPrice(totalPrice)
            .totalCount(cartItems.size())
            .cartItems(cartItemDtoList)
            .build();
  }

  /**
   * 장바구니에 강의(classId)를 추가
   * 이미 결제된 강의나 이미 담긴 강의는 예외 발생
   */
  @Transactional
  public CartResponseDto cartItemAdd(Long userId, Long classId) {
    Cart cart = getOrCreateCart(userId);

    // 이미 결제한 강의는 담을 수 없음
    List<PaymentItem> paidItems = paymentItemRepository.findByUserId(userId);
    boolean alreadyPurchased = paidItems.stream()
            .filter(item -> !"CANCELED".equalsIgnoreCase(item.getPayment().getStatus().getName()))
            .anyMatch(item -> item.getClasses().getId().equals(classId));

    if (alreadyPurchased) {
      throw new IllegalStateException("이미 결제한 강의는 장바구니에 담을 수 없습니다.");
    }

    // 이미 장바구니에 담긴 강의인지 체크
    boolean exists = cartItemRepository.existsByCartIdAndClassesId(cart.getId(), classId);
    if (exists) {
      throw new IllegalStateException("이미 장바구니에 담긴 강의입니다.");
    }

    // 장바구니에 강의 추가
    Classes classEntity = classesRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("클래스를 찾을 수 없습니다."));

    CartItem cartItem = new CartItem();
    cartItem.setCart(cart);
    cartItem.setClasses(classEntity);
    cartItem.setAmount(classEntity.getClassPrice());
    cartItem.setAdded_at(LocalDateTime.now());
    cartItemRepository.save(cartItem);

    return CartResponseDto.builder()
            .message("장바구니에 강의가 추가되었습니다.")
            .status("success")
            .build();
  }

  /**
   * 장바구니에서 선택한 강의 아이템들을 제거
   * 실패 시 에러 메시지 반환
   */
  @Transactional
  public CartResponseDto cartItemRemove(Long userId, CartItemIdsDto cartItemIdsDto) {
    List<Long> ids = cartItemIdsDto.getCartItemIds();
    if (ids == null || ids.isEmpty()) {
      return CartResponseDto.builder()
              .message("삭제할 클래스가 없습니다.")
              .status("failed")
              .build();
    }

    try {
      cartItemRepository.deleteAllByIdInBatch(ids);
      return CartResponseDto.builder()
              .message("선택한 클래스들이 삭제되었습니다.")
              .status("success")
              .items(cartItemIdsDto)
              .build();
    } catch (Exception e) {
      return CartResponseDto.builder()
              .message("선택한 클래스 삭제에 실패했습니다.")
              .status("failed")
              .items(cartItemIdsDto)
              .build();
    }
  }

  /**
   * 장바구니 전체 비우기
   * 장바구니가 없거나 삭제 중 에러가 발생하면 실패 메시지 반환
   */
  @Transactional
  public CartResponseDto cartItemAllRemove(Long userId) {
    try {
      Cart cart = cartRepository.findByUserId(userId);

      if (cart == null) {
        return CartResponseDto.builder()
                .message("장바구니가 존재하지 않습니다.")
                .status("failed")
                .build();
      }

      cartItemRepository.deleteByCartId(cart.getId());

      return CartResponseDto.builder()
              .message("장바구니가 비워졌습니다.")
              .status("success")
              .build();
    } catch (Exception e) {
      return CartResponseDto.builder()
              .message("장바구니 비우기에 실패했습니다.")
              .status("failed")
              .build();
    }
  }
}
