package com.example.musica_be.service.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.*;
import com.example.musica_be.repository.classes.CategoryRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    // 클래스 등록
    @Transactional
    public Long createClass(ClassCreateReqDto dto, String jwt) {
        // 사용자 아이디 추출 (userId)
        Long userId = JwtUtils.extractUserId(jwt);
        // 존재하는 사용자인지 확인
        User instructor = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // 클래스 등록 권한 확인
        validateInstructorByRole(jwt);
        // 존재하는 난이도인지 확인
        Level difficulty = levelRepository.findById(dto.getDifficultyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 난이도 ID입니다."));
        // 존재하는 카테고리인지 확인
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // dto 에서 전달받은 내용으로 새 클래스 생성 (빌더 패턴)
        Classes classes = Classes.builder()
            .title(dto.getTitle())
            .descriptionHtml(dto.getDescriptionHtml())
            .category(category)
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
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 클래스인지 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));
        // 유저 권한 확인
        validateInstructorByClassAndUserId(classes, userId);
        // 존재하는 난이도인지 확인
        Level difficulty = levelRepository.findById(dto.getDifficultyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 난이도입니다."));
        // 존재하는 카테고리인지 확인
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 엔티티 수정
        classes.update(
            dto.getTitle(),
            dto.getDescriptionHtml(),
            category,
            difficulty,
            dto.getThumbnailUrl(),
            dto.getClassPrice()
        );
    }

    // 클래스 삭제
    @Transactional
    public void deleteClass(Long classId, String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 클래스인지 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));
        // 유저 권한 확인
        validateInstructorByClassAndUserId(classes, userId);

        classesRepository.delete(classes);
    }

    // 클래스 상세 조회
    @Transactional(readOnly = false)
    public ClassDetailResDto getClassDetail(String jwt, Long classId) {
        increaseViewCount(classId); // 클래스 조회수 증가

        // 1. 클래스 조회
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        // 2. 사용자 조회
        User user = userRepository.findById(JwtUtils.extractUserId(jwt))
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ClassDetailResDto.from(classes);
    }

    // 클래스 목록 조회
    // 1. 클래스 검색 결과 - 비회원 및 공개용
    /**
     * 클래스 검색 및 필터링 결과를 반환하는 메서드
     *
     * @param keyword      검색어 (제목, 설명 등에 포함된 키워드)
     * @param categoryId   카테고리 ID 필터 (nullable)
     * @param difficultyId 난이도(Level) ID 필터 (nullable)
     * @param sortList     정렬 조건 리스트 (예: ["rating", "priceAsc"])
     * @return             필터링 및 정렬된 클래스 요약 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> searchFilteredClassList(
        String keyword, Long categoryId, Long difficultyId, List<String> sortList
    ) {
        // 1. 필터 조건(keyword, categoryId, difficultyId)에 해당하는 클래스 목록 조회
        List<Classes> filtered = classesRepository.findByConditions(keyword, categoryId, difficultyId);

        // 2. 클래스별 수강생 수, 평균 별점 정보를 미리 Map 형태로 조회 (성능 최적화)
        Map<Long, Integer> studentCountMap = getStudentCountMap(filtered);
        Map<Long, Double> ratingMap = getRatingMap(filtered);

        // 3. 정렬 기준에 따라 Comparator 누적 생성
        Comparator<Classes> comparator = null;

        for (String sort : sortList) {
            Comparator<Classes> current =

            // 각 정렬 조건별 Comparator 정의
            switch (sort) {
                case "popular" -> // 조회수 많은 순
                    current = Comparator.comparingInt(Classes::getViewCount).reversed();

                case "priceAsc" -> // 가격 낮은 순
                    current = Comparator.comparingInt(Classes::getClassPrice);

                case "priceDesc" -> // 가격 높은 순
                    current = Comparator.comparingInt(Classes::getClassPrice).reversed();

                case "latest" -> // 최신 등록일 순
                    current = Comparator.comparing(Classes::getCreatedAt).reversed();

                case "students" -> // 수강생 많은 순
                    current = Comparator.comparingInt(
                    (Classes c) -> studentCountMap.getOrDefault(c.getId(), 0)
                ).reversed();

                case "rating" // 평균 별점 높은 순
                    -> current = Comparator.comparingDouble(
                    (Classes c) -> ratingMap.getOrDefault(c.getId(), 0.0)
                ).reversed();
                default -> null;
            };

            if (current == null) {
                continue;
            }

            // 기존 comparator와 현재 comparator를 연결 (우선순위대로 정렬)
            if (comparator == null) {
                comparator = current; // 첫 정렬 기준
            } else {
                comparator = comparator.thenComparing(current); // 이후 조건 이어붙이기
            }
        }

        // 4. 최종 comparator를 기준으로 정렬 수행
        if (comparator != null) {
            filtered.sort(comparator);
        } else {
            // 정렬 조건이 없을 경우 기본 정렬: 최신순
            filtered.sort(Comparator.comparing(Classes::getCreatedAt).reversed());
        }

        // 5. 결과 클래스들을 DTO로 변환 (강의 수 포함)
        return filtered.stream()
            .map(c -> {
                int lectureCount = lectureRepository.countByClassesId(c.getId());
                return ClassSummaryDto.from(c, lectureCount);
            })
            .toList();
    }

    // 2. 수강생용 - 본인이 수강하는 클래스만
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> getClassListForStudent(String jwt) {
        // 1. JWT 에서 유저 ID 추출
        Long userId = JwtUtils.extractUserId(jwt);

        // 2. 유저가 결제한 클래스 목록 조회 (취소된 결제 제외)
        List<Classes> enrolledClasses = classesRepository.findEnrolledClassesByUserId(userId);

        // 3. 각 클래스에 대해 강의 수 조회 후 DTO 로 변환
        return enrolledClasses.stream()
            .map(c -> {
                int lectureCount = lectureRepository.countByClassesId(c.getId());
                return ClassSummaryDto.from(c, lectureCount);
            })
            .toList();
    }

    // 3. 강사용 - 본인이 올린 클래스만
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> getClassListForInstructor(String jwt) {
        // 1. 유저가 강사인지 확인
        validateInstructorByRole(jwt);

        // 2. JWT 에서 userId 추출
        Long userId = JwtUtils.extractUserId(jwt);

        // 3. 강사가 등록한 클래스 목록 조회
        List<Classes> instructorClasses = classesRepository.findByInstructorId(userId);

        // 4. 각 클래스에 대한 강의 수를 조회한 후 DTO 로 변환
        return instructorClasses.stream()
            .map(c -> {
                int lectureCount = lectureRepository.countByClassesId(c.getId());
                return ClassSummaryDto.from(c, lectureCount);
            })
            .toList();
    }

    // ====== 헬퍼 메서드 ======
    /**
     * 클래스의 소유자인 강사만 수정/삭제할 수 있도록 검증하는 메서드
     *
     * @param classes 대상 클래스 엔티티
     * @param userId 현재 로그인한 유저 ID (JWT에서 추출된 값)
     * @throws SecurityException 만약 클래스의 강사 ID와 현재 유저 ID가 다르면 예외 발생
     *
     * 사용 예시:
     * - 클래스 수정, 삭제 API에서 사용
     * - 해당 클래스의 소유자가 본인인지 확인할 때
     */
    private void validateInstructorByClassAndUserId(Classes classes, Long userId) {
        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("권한이 없습니다: 강사 본인의 클래스만 수정/삭제할 수 있습니다.");
        }
    }

    /**
     * 요청한 유저가 '강사(INSTRUCTOR)' 권한을 가지고 있는지 검증하는 메서드
     *
     * @param jwt 클라이언트 요청의 JWT 토큰
     * @throws SecurityException 만약 해당 유저의 역할이 강사가 아니면 예외 발생
     *
     * 사용 예시:
     * - 클래스 등록 API에서 사용
     * - 강사만 접근 가능한 기능을 호출했는지 사전 체크할 때
     */
    private void validateInstructorByRole(String jwt) {
        String role = JwtUtils.extractRole(jwt);
        System.out.println("role = " + role); // 디버깅용 출력
        if (!"INSTRUCTOR".equals(role)) {
            throw new SecurityException("권한이 없습니다: 강사가 아닙니다.");
        }
    }

    /**
     * 클래스 ID 목록을 기준으로 수강생 수를 조회하여 Map으로 반환합니다.
     *
     * @param classesList 클래스 엔티티 리스트
     * @return 클래스 ID → 수강생 수 (Integer)로 구성된 Map
     *
     * 사용 예:
     * - 수강생 수 기준 정렬("students") 시 사용
     */
    private Map<Long, Integer> getStudentCountMap(List<Classes> classesList) {
        return classesRepository.countStudentsByClassIds(
            classesList.stream().map(Classes::getId).toList()
        ).stream().collect(Collectors.toMap(
            StudentCountDto::getClassId, // 클래스 ID를 key로
            dto -> dto.getCount().intValue() // 수강생 수(Long)을 int로 변환해서 value로
        ));
    }

    /**
     * 클래스 ID 목록을 기준으로 평균 별점을 조회하여 Map으로 반환합니다.
     *
     * @param classesList 클래스 엔티티 리스트
     * @return 클래스 ID → 평균 별점(Double)으로 구성된 Map
     *
     * 사용 예:
     * - 평점 기준 정렬("rating") 시 사용
     */
    private Map<Long, Double> getRatingMap(List<Classes> classesList) {
        return reviewRepository.getAverageRatingsByClassIds(
            classesList.stream().map(Classes::getId).toList()
        ).stream().collect(Collectors.toMap(
            ClassesRatingAvgDto::getClassId,
            ClassesRatingAvgDto::getAverageRating
        ));
    }

    /**
     * 클래스 조회수 증가 메서드
     *
     * 클래스 상세 페이지를 조회할 때 호출되어, 해당 클래스의 조회수(viewCount)를 1 증가시킨다.
     * JPA의 Dirty Checking을 활용하므로 save() 호출 없이도 변경 사항이 반영된다.
     *
     * @param classId 조회수를 증가시킬 클래스의 ID
     * @throws IllegalArgumentException 주어진 ID의 클래스가 존재하지 않을 경우 예외 발생
     */
    private void increaseViewCount(Long classId) {
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("해당 클래스가 존재하지 않습니다."));

        classes.setViewCount(classes.getViewCount() + 1);
    }

}
