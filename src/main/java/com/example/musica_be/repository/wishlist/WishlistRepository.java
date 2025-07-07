package com.example.musica_be.repository.wishlist;

import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    boolean existsByUserAndClasses(User user, Classes classes);
}
