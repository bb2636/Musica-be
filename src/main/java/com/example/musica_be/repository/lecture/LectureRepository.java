package com.example.musica_be.repository.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.dto.classes.ClassStatisticsDto;
import com.example.musica_be.dto.classes.ClassesLectureCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    int countByClassesId(Long classId);

    int countByClasses(Classes classes);

    List<Lecture> findByClasses(Classes classes);

    List<Lecture> findByClassesId(Long classId);

    @Query("""
            SELECT new com.example.musica_be.dto.classes.ClassesLectureCountDto(l.classes.id, COUNT(l))
            FROM Lecture l
            WHERE l.classes.id IN :classIds
            GROUP BY l.classes.id
        """)
    List<ClassesLectureCountDto> countLecturesByClassIds(@Param("classIds") List<Long> classIds);
}