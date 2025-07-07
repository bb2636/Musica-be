package com.example.musica_be.service.cart;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.cart.CartDto;
import com.example.musica_be.dto.cart.CartItemDto;
import com.example.musica_be.dto.cart.CartItemIdsDto;
import com.example.musica_be.dto.cart.CartResponseDto;
import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final UserRepository userRepository;
  private final ClassesRepository classesRepository;

  @Transactional
  // 유저의 카트 정보
  public CartDto getCart(String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    Cart cart = cartRepository.findByUserId(userId);

    //카트가 없을 경우 유저의 카트 추가
    if (cart == null) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));

      cart = new Cart();
      cart.setUser(user);
      cart.setCreated_at(new Timestamp(System.currentTimeMillis()));
      cartRepository.save(cart);
    }

    //CartDto 생성
    List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
    List<CartItemDto> cartItemDtoList = new ArrayList<CartItemDto>();
    for (CartItem cartItem : cartItems) {
      Classes classes = classesRepository.findById(cartItem.getClasses().getId())
          .orElseThrow(() -> new RuntimeException("클래스를 찾을 수 없습니다."));

      CartItemDto dto = CartItemDto.builder()
          .classId(classes.getId())
          .title(classes.getTitle())
          .thumbnailUrl(classes.getThumbnailUrl())
          .price(classes.getClassPrice())
          .build();

      cartItemDtoList.add(dto);
    }
    CartItemDto.builder()
        .build();

    return CartDto.builder()
        .userId(userId)
        .cartItems(cartItemDtoList)
        .build();
  }

  // 카트 추가
  @Transactional
  public void cartAdd(Long classId, String jwt) {
    Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    Cart cart = cartRepository.findByUserId(userId);

    // 클래스가 이미 장바구니에 담겼는지 체크
    boolean exists = cartItemRepository.existsByCartIdAndClassId(cart.getId(), classId);
    if (exists) {
      throw new IllegalStateException("이미 장바구니에 담긴 강의입니다.");
    }

    Classes classEntity = classesRepository.findById(classId)
        .orElseThrow(() -> new RuntimeException("클래스를 찾을 수 없습니다."));

    CartItem cartItem = new CartItem();
    cartItem.setCart(cart);
    cartItem.setClasses(classEntity);
    cartItemRepository.save(cartItem);
  }

  @Transactional
  public CartResponseDto cartListRemove(String jwt, CartItemIdsDto cartItemIdsDto) {
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

  @Transactional
  public CartResponseDto cartAllRemove(String jwt) {
    try {
      Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
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
