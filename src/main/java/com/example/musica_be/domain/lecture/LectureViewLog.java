package com.example.musica_be.domain.lecture;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class LectureViewLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecture_id", nullable = false)
  Lecture lecture;
  LocalDate watched_at;
  LocalTime duration_seconds;

  public int getDurationSeconds() {
    return duration_seconds != null ? duration_seconds.toSecondOfDay() : 0;
  }
}
