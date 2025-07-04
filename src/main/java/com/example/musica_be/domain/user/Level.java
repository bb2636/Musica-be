package com.example.musica_be.domain.user;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; //레벨 이름 (Beginner, Intermediate, Advanced)
}
