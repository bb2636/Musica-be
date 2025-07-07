package com.example.musica_be.repository.cart;

import com.example.musica_be.domain.payment.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
