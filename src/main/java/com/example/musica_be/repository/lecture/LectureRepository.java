package com.example.musica_be.repository.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    int countByClassesId(Long classId);
    int countByClasses(Classes classes);
    List<Lecture> findByClasses(Classes classes);
    List<Lecture> findByClassesId(Long classId);
}