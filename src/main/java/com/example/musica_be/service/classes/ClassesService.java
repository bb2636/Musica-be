package com.example.musica_be.service.classes;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.ClassDetailResDto;
import com.example.musica_be.dto.classes.ClassCreateReqDto;
import com.example.musica_be.dto.classes.ClassSummaryDto;
import com.example.musica_be.dto.classes.ClassUpdateReqDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;

    // 클래스 등록
    @Transactional
    public Long createClass(ClassCreateReqDto dto, String jwt) {
        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        String userIdStr = JwtUtils.getUserIdFromToken(token);
        Long userId = Long.parseLong(userIdStr);

        User instructor = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Level difficulty = levelRepository.findById(dto.getDifficultyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 난이도 ID입니다."));

        Classes classes = Classes.builder()
            .title(dto.getTitle())
            .descriptionHtml(dto.getDescriptionHtml())
            .category(dto.getCategory())
            .difficulty(difficulty)
            .thumbnailUrl(dto.getThumbnailUrl())
            .classPrice(dto.getClassPrice())
            .instructor(instructor)
            .build();

        return classesRepository.save(classes).getId();
    }

    // 클래스 수정
    @Transactional
    public void updateClass(Long classId, ClassUpdateReqDto dto, String jwt) {
        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        Long userId = Long.parseLong(JwtUtils.getUserIdFromToken(token));

        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));

        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        Level difficulty = levelRepository.findById(dto.getDifficultyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 난이도 ID입니다."));

        // 엔티티 수정
        classes.update(
            dto.getTitle(),
            dto.getDescriptionHtml(),
            dto.getCategory(),
            difficulty,
            dto.getThumbnailUrl(),
            dto.getClassPrice()
        );
    }

    // 클래스 삭제
    @Transactional
    public void deleteClass(Long classId, String jwt) {
        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        Long userId = Long.parseLong(JwtUtils.getUserIdFromToken(token));

        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));

        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        classesRepository.delete(classes);
    }

    // 클래스 단건 조회 (상세조회)
    @Transactional(readOnly = true)
    public ClassDetailResDto getClassDetail(Long classId) {
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("해당 클래스가 존재하지 않습니다."));
        return ClassDetailResDto.from(classes);
    }

    // 클래스 목록 조회
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> getAllClasses() {
        List<Classes> result = classesRepository.findAll();

        return result.stream()
            .map(classes -> {
                int lectureCount = lectureRepository.countByClasses(classes);
                return ClassSummaryDto.from(classes, lectureCount);
            })
            .toList();
    }
}
