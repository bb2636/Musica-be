package com.example.musica_be.domain;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "wishlist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "class_id"})
)
public class Wishlist {

    @Id
    @Column(name = "wishlist_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wishlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Classes classes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters

}
