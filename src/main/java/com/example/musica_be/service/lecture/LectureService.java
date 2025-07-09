package com.example.musica_be.service.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final ClassesRepository classesRepository;

    private final UserRepository userRepository;

    /**
     * 강의 등록
     */
    @Transactional
    public Long createLecture(String jwt, Long classId, LectureCreateReqDto dto) {
        Long userId = extractUserIdFromJwt(jwt);

        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        validateInstructor(classes, userId);

        Lecture lecture = Lecture.builder()
            .classes(classes)
            .title(dto.getTitle())
            .videoUrl(dto.getVideoUrl())
            .progress(0)
            .sheetMusicUrl(dto.getSheetMusicUrl())
            .lectureOrder(dto.getLectureOrder())
            .build();

        return lectureRepository.save(lecture).getId();
    }

    /**
     * 강의 수정
     */
    @Transactional
    public void updateLecture(String jwt, Long lectureId, LectureUpdateReqDto dto) {
        Long userId = extractUserIdFromJwt(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        validateInstructor(lecture.getClasses(), userId);

        lecture.update(
            dto.getTitle(),
            dto.getVideoUrl(),
            dto.getSheetMusicUrl(),
            dto.getLectureOrder()
        );
    }

    /**
     * 강의 삭제
     */
    @Transactional
    public void deleteLecture(String jwt, Long lectureId) {
        Long userId = extractUserIdFromJwt(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        validateInstructor(lecture.getClasses(), userId);

        lectureRepository.delete(lecture);
    }

    /**
     * 강의 상세 조회
     */
    @Transactional(readOnly = true)
    public LectureDetailResDto getLectureDetail(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        return LectureDetailResDto.from(lecture);
    }

    /**
     * 강의 목록 조회 (classId 기준)
     */
    @Transactional(readOnly = true)
    public List<LectureSummaryDto> getLectureList(Long classId) {
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        List<Lecture> lectures = lectureRepository.findByClasses(classes);

        return lectures.stream()
            .sorted(Comparator.comparingInt(Lecture::getLectureOrder))
            .map(LectureSummaryDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 강의 시청 (간단히 videoUrl과 진행률만 응답)
     */
    @Transactional(readOnly = true)
    public LectureWatchResDto watchLecture(String jwt, Long lectureId) {
        // 유저 인증 여부는 단순 확인용 (특별한 로직 없이)
        extractUserIdFromJwt(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        return LectureWatchResDto.from(lecture);
    }

    /**
     * 강의 순서 변경
     */
    @Transactional
    public void updateLectureOrder(String jwt, Long classId, LectureOrderUpdateReqDto dto) {
        Long userId = extractUserIdFromJwt(jwt);

        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        validateInstructor(classes, userId);

        for (LectureOrderUpdateReqDto.LectureOrderDto item : dto.getOrders()) {
            Lecture lecture = lectureRepository.findById(item.getLectureId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

            if (!lecture.getClasses().getId().equals(classId)) {
                throw new IllegalArgumentException("강의가 해당 클래스에 속하지 않습니다.");
            }

            lecture.changeOrder(item.getOrder());
        }
    }

    /**
     * 강의 진행률 저장
     */
    @Transactional
    public void saveProgress(String jwt, Long lectureId, LectureProgressSaveReqDto dto) {
        Long userId = extractUserIdFromJwt(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 권한 검사 필요 여부에 따라 분기 (지금은 instructor만 수정 가능하도록 제한 안 함)
        if (!lecture.getId().equals(dto.getLectureId())) {
            throw new IllegalArgumentException("요청한 lectureId와 DTO의 ID가 다릅니다.");
        }

        lecture.updateProgress(dto.getProgress());
    }

    // ====== 🔧 헬퍼 메서드 ======
    private Long extractUserIdFromJwt(String jwt) {
        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        return Long.parseLong(JwtUtils.getUserIdFromToken(token));
    }

    private void validateInstructor(Classes classes, Long userId) {
        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("권한이 없습니다.");
        }
    }
}