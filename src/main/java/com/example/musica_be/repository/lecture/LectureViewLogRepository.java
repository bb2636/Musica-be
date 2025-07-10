package com.example.musica_be.repository.lecture;

import com.example.musica_be.domain.lecture.LectureViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureViewLogRepository extends JpaRepository<LectureViewLog, Long> {
  List<LectureViewLog> findByUserIdAndLecture_Classes_Id(Long userId, Long classId);
}
