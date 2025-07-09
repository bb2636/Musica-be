package com.example.musica_be.domain.question;

import com.example.musica_be.domain.answer.Answer;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionStatus status; // ANSWERED, IN_PROGRESS

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = QuestionStatus.IN_PROGRESS;
        }
    }
}

