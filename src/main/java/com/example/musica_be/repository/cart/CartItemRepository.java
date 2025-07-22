package com.example.musica_be.repository.cart;

import com.example.musica_be.domain.cart.Cart;
import com.example.musica_be.domain.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  void deleteByCartId(Long cartId);

  boolean existsByCartIdAndClassesId(Long cartId, Long classesId);


  @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.classes WHERE ci.cart.id = :cartId")
  List<CartItem> findAllByCartIdWithClasses(@Param("cartId") Long cartId);

  List<CartItem> findByCart(Cart cart);

  @Modifying
  @Transactional
  @Query("DELETE FROM CartItem ci WHERE ci.classes.id = :classId")
  void deleteByClassId(@Param("classId") Long classId);
}
