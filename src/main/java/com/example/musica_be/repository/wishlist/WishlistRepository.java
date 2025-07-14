package com.example.musica_be.repository.wishlist;

import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    boolean existsByUserAndClasses(User user, Classes classes);
    Optional<Wishlist> findByUserAndClasses(User user, Classes classes);
    List<Wishlist> findAllByUser(User user);

    List<Wishlist> findByUserId(Long userId);

    int countByClasses(Classes classes);  // 찜 수
}
