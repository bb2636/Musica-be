package com.example.musica_be.repository.cart;

import com.example.musica_be.domain.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  void deleteByCartId(Long cartId);
<<<<<<< HEAD
  boolean existsByCartIdAndClassesId(Long cartId, Long classId);
=======
  boolean existsByCartIdAndClassesId(Long cartId, Long classesId);
>>>>>>> f240ed395dd8c8f05f501b7e0fc6e605ca16490d

  @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.classes WHERE ci.cart.id = :cartId")
  List<CartItem> findAllByCartIdWithClasses(@Param("cartId") Long cartId);
}
