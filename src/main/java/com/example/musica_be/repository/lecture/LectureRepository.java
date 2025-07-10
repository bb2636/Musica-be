package com.example.musica_be.repository.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    int countByClasses(Classes classes);
    List<Lecture> findByClasses(Classes classes);
    List<Lecture> findByClassesId(Long classId);
}