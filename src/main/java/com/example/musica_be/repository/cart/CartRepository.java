package com.example.musica_be.repository.cart;

import com.example.musica_be.domain.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
  Cart findByUserId(Long userId);
}
