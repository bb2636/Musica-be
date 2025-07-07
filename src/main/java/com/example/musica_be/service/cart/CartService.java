package com.example.musica_be.service.cart;

import com.example.musica_be.repository.cart.CartItemRepository;
import com.example.musica_be.repository.cart.CartRepository;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  // 유저의 카트 정보
  public void cartSearch(){
  }

  // 카트 추가
  public void  cartAdd(){
  }

  public void cartRemove(){
  }

  public void cartAllRemove(Jwts jwts){
    cartRepository.deleteById();
  }
}
