package com.example.musica_be.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Wishlist {

    @Id
    @Column(name = "wishlist_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wishlistId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.example.musica_be.domain.user.User user;

//    @ManyToOne
//    @JoinColumn(name = "class_id", nullable = false)
//    private ClassEntity classEntity;
    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters

}
