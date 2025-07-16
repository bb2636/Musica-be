package com.example.musica_be.service.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.*;
import com.example.musica_be.dto.lecture.LectureSummaryDto;
import com.example.musica_be.repository.classes.CategoryRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureProgressRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.payment.PaymentRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.repository.wishlist.WishlistRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final WishlistRepository wishlistRepository;

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
        increaseViewCount(classId); // 1. 조회수 증가

        // 2. 클래스 조회
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        // 3. 로그인 사용자 조회
        User user = null;
        if (jwt != null && jwt.startsWith("Bearer ")) {
            Long userId = JwtUtils.extractUserId(jwt);
            if (userId != null) {
                user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            }
        }

        // 4. 강의 목록 생성
        List<Lecture> lectures = classes.getLectures().stream()
            .sorted(Comparator.comparing(Lecture::getLectureOrder))
            .toList();

        List<LectureSummaryDto> lectureDtos;

        // 5. 로그인 사용자인 경우: LectureProgress 함께 조회
        if (user != null) {
            // 한번에 전체 강의 진도 정보 조회
            List<LectureProgress> progresses = lectureProgressRepository
                .findAllByUserIdAndClassId(user.getId(), classId);

            Map<Long, LectureProgress> progressMap = progresses.stream()
                .collect(Collectors.toMap(p -> p.getLecture().getId(), p -> p));

            lectureDtos = lectures.stream()
                .map(lecture -> LectureSummaryDto.from(lecture, progressMap.get(lecture.getId())))
                .collect(Collectors.toList());

            // 수강 여부 및 전체 진도 계산
            boolean isEnrolled = paymentItemRepository.existsByPayment_User_IdAndClasses_Id(user.getId(), classId);
            int totalCount = lectures.size();
            int completedCount = (int) progresses.stream().filter(LectureProgress::getIsCompleted).count();
            double progressRate = totalCount > 0 ? ((double) completedCount / totalCount * 100) : 0.0;

            ClassDetailResDto.UserClassStatus userStatus = ClassDetailResDto.UserClassStatus.builder()
                .isEnrolled(isEnrolled)
                .completedLectureCount(completedCount)
                .totalLectureCount(totalCount)
                .progressRate(progressRate)
                .build();

            return ClassDetailResDto.builder()
                .id(classes.getId())
                .title(classes.getTitle())
                .descriptionHtml(classes.getDescriptionHtml())
                .categoryName(classes.getCategory().getDisplayName())
                .difficulty(classes.getDifficulty().getName())
                .thumbnailUrl(classes.getThumbnailUrl())
                .classPrice(classes.getClassPrice())
                .instructorName(classes.getInstructor().getName())
                .userClassStatus(userStatus)
                .lectures(lectureDtos)
                .build();
        }

        // 6. 비로그인 사용자용
        lectureDtos = lectures.stream()
            .map(LectureSummaryDto::from)
            .collect(Collectors.toList());

        return ClassDetailResDto.builder()
            .id(classes.getId())
            .title(classes.getTitle())
            .descriptionHtml(classes.getDescriptionHtml())
            .categoryName(classes.getCategory().getDisplayName())
            .difficulty(classes.getDifficulty().getName())
            .thumbnailUrl(classes.getThumbnailUrl())
            .classPrice(classes.getClassPrice())
            .instructorName(classes.getInstructor().getName())
            .userClassStatus(null)
            .lectures(lectureDtos)
            .build();
    }

    // 클래스 목록 조회
    // 1. 클래스 검색 결과 - 공개용
    @Transactional(readOnly = true)
    public Page<ClassSummaryDto> searchFilteredClassList(
        String keyword,
        Long categoryId,
        Long difficultyId,
        List<String> sortList,
        int page,
        int size
    ) {
        // 1. Sort는 엔티티 필드 기준으로만 세팅 (studentCount/rating 제외)
        Sort sort = getSort(sortList);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. DB에서 클래스 페이지 조회 (정렬은 createdAt, viewCount, price 등만 가능)
        Page<Classes> classPage = classesRepository.searchFiltered(keyword, categoryId, difficultyId, pageable);

        // 3. 통계 정보 수집
        Map<Long, ClassStatisticsDto> statsMap = getClassStatisticsMap(classPage.getContent());

        // 4. DTO 변환
        List<ClassSummaryDto> dtoList = classPage.getContent().stream()
            .map(c -> ClassSummaryDto.from(c, statsMap.get(c.getId())))
            .collect(Collectors.toList());

        // 5. 💡 수동 정렬 적용 (studentCount, averageRating)
        for (String sortKey : sortList) {
            switch (sortKey) {
                case "students" -> dtoList.sort(Comparator
                    .comparingInt(ClassSummaryDto::getStudentCount).reversed());

                case "rating" -> dtoList.sort(Comparator
                    .comparingDouble(ClassSummaryDto::getAverageRating).reversed());
            }
        }

        // 6. 정렬된 리스트로 PageImpl 생성 후 반환
        return new PageImpl<>(dtoList, pageable, classPage.getTotalElements());
    }

    // 2. 수강생용 - 본인이 수강하는 클래스만
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> getClassListForStudent(String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);

        List<Classes> enrolledClasses = classesRepository.findEnrolledClassesByUserId(userId);

        Map<Long, ClassStatisticsDto> statsMap = getClassStatisticsMap(enrolledClasses);

        return enrolledClasses.stream()
            .map(c -> ClassSummaryDto.from(c, statsMap.get(c.getId())))
            .toList();
    }

    // 3. 강사용 - 본인이 올린 클래스만
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> getClassListForInstructor(String jwt) {
        validateInstructorByRole(jwt);
        Long userId = JwtUtils.extractUserId(jwt);

        List<Classes> instructorClasses = classesRepository.findByInstructorId(userId);

        Map<Long, ClassStatisticsDto> statsMap = getClassStatisticsMap(instructorClasses);

        return instructorClasses.stream()
            .map(c -> ClassSummaryDto.from(c, statsMap.get(c.getId())))
            .toList();
    }

    // 같은레벨 추천클래스(20개) - 평점기준 탑20개
    @Transactional(readOnly = true)
    public List<ClassCardDto> getRecommendedClasses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Long levelId = Optional.ofNullable(user.getLevel())
                .map(Level::getId)
                .orElseThrow(() -> new IllegalStateException("회원 레벨 정보가 없습니다."));

        Pageable top20 = PageRequest.of(0, 20);
        List<Classes> recommended = classesRepository.findRecommendedByLevelId(levelId, top20);

        if (recommended.size() < 20) {
            List<Long> excludeIds = recommended.stream().map(Classes::getId).toList();
            List<Classes> latest = classesRepository.findTop20ByOrderByCreatedAtDesc().stream()
                    .filter(c -> !excludeIds.contains(c.getId()))
                    .limit(20 - recommended.size())
                    .toList();
            recommended.addAll(latest);
        }

        Map<Long, ClassCardStatisticsDto> statsMap = getClassCardStats(recommended);
        return recommended.stream()
                .map(cls -> ClassCardDto.from(cls, statsMap.get(cls.getId())))
                .toList();
    }


    // Popularity 점수 기반 인기 클래스 조회
    @Transactional(readOnly = true)
    public List<ClassCardDto> getPopularClasses() {
        List<Classes> allClasses = classesRepository.findAll();

        Map<Long, ClassCardStatisticsDto> statsMap = getClassCardStats(allClasses);

        List<ClassPopularityDto> scoredList = allClasses.stream()
                .map(c -> {
                    ClassCardStatisticsDto stats = statsMap.getOrDefault(c.getId(), new ClassCardStatisticsDto());
                    int score = (int) (stats.getStudentCount() * 2 + stats.getWishlistCount());
                    return new ClassPopularityDto(c, score);
                })
                .sorted(Comparator.comparingInt(ClassPopularityDto::getScore).reversed()
                        .thenComparing(a -> a.getClasses().getCreatedAt(), Comparator.reverseOrder()))
                .limit(20)
                .toList();

        return scoredList.stream()
                .map(dto -> ClassCardDto.from(dto.getClasses(), statsMap.get(dto.getClasses().getId())))
                .toList();
    }

    // 최신 클래스 (20개 limit)
    @Transactional(readOnly = true)
    public List<ClassCardDto> getLatestClasses() {
        List<Classes> latestClasses = classesRepository.findTop20ByOrderByCreatedAtDesc();
        Map<Long, ClassCardStatisticsDto> statsMap = getClassCardStats(latestClasses);

        return latestClasses.stream()
                .map(cls -> ClassCardDto.from(cls, statsMap.get(cls.getId())))
                .toList();
    }

    // ====== 헬퍼 메서드 ======

    /**
     * 클래스의 소유자인 강사만 수정/삭제할 수 있도록 검증하는 메서드
     *
     * @param classes 대상 클래스 엔티티
     * @param userId  현재 로그인한 유저 ID (JWT에서 추출된 값)
     * @throws SecurityException 만약 클래스의 강사 ID와 현재 유저 ID가 다르면 예외 발생
     *                           <p>
     *                           사용 예시:
     *                           - 클래스 수정, 삭제 API에서 사용
     *                           - 해당 클래스의 소유자가 본인인지 확인할 때
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
     *                           <p>
     *                           사용 예시:
     *                           - 클래스 등록 API에서 사용
     *                           - 강사만 접근 가능한 기능을 호출했는지 사전 체크할 때
     */
    private void validateInstructorByRole(String jwt) {
        String role = JwtUtils.extractRole(jwt);
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
            ClassesStudentCountDto::getClassId, // 클래스 ID를 key로
            dto -> dto.getStudentCount().intValue() // 수강생 수(Long)을 int로 변환해서 value로
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

    // 클래스별 평균 별점 구하는 로직 - (FE) ClassCard 표시용
    /**
     * 특정 클래스(classId)의 평균 별점을 계산하여 반환하는 메서드입니다.
     *
     * 이 메서드는 클래스 카드(ClassCardDto)를 구성할 때, 사용자들에게 보여질 평균 별점 정보를 제공하기 위해 사용됩니다.
     * 평균 별점은 Review 테이블에서 해당 클래스에 작성된 모든 리뷰의 별점을 기반으로 계산됩니다.
     *
     * 사용 시점:
     * - 추천 클래스 조회 (getRecommendedClasses)
     * - 인기 클래스 조회 (getPopularClasses)
     * - 최신 클래스 조회 (getLatestClasses)
     * 등에서 ClassCardDto 생성 시 사용됩니다.
     *
     * @param classId 평균 별점을 계산할 대상 클래스의 ID
     * @return 평균 별점 값 (리뷰가 없을 경우 기본값 0.0)
     */
    private double calculateAvgRating(Long classId) {
        return reviewRepository.calculateAverageRatingByClassId(classId).orElse(0.0);
    }

    /**
     * 무료 클래스 카드 리스트를 조회하는 메서드
     * - 클래스 가격이 0원인 최신 클래스 5개를 조회
     * - 각 클래스에 대해 평균 별점을 계산하여 DTO로 변환
     *
     * @return 무료 클래스 정보를 담은 ClassCardDto 리스트
     */
    @Transactional(readOnly = true)
    public List<ClassCardDto> getFreeClassCards() {
        // 가격이 0원인 클래스 중, 생성일 기준 내림차순으로 최대 5개 조회
        List<Classes> freeClasses = classesRepository.findTop5ByClassPriceOrderByCreatedAtDesc(0);

        Map<Long, ClassCardStatisticsDto> statsMap = getClassCardStats(freeClasses);

        return freeClasses.stream()
                .map(cls -> ClassCardDto.from(cls, statsMap.get(cls.getId())))
                .toList();
    }

    /**
     * 정렬 조건 리스트를 기반으로 Spring Data의 Sort 객체를 생성하는 메서드
     * - 프론트엔드에서 전달받은 정렬 조건 문자열을 기준으로 정렬 순서를 정의
     *
     * 지원하는 정렬 옵션:
     * - "popular": 조회수(viewCount) 내림차순
     * - "priceAsc": 가격(classPrice) 오름차순
     * - "priceDesc": 가격(classPrice) 내림차순
     * - "latest": 최신순(createdAt) 내림차순
     * - "students": 수강생 수(studentCount) 내림차순 → 해당 컬럼이 Entity에 존재해야 함
     * - "rating": 평균 별점(averageRating) 내림차순 → 해당 컬럼이 Entity에 존재해야 함
     *
     * @param sortList 정렬 기준 문자열 리스트
     * @return 적용할 정렬 조건을 포함한 Sort 객체
     */
    private Sort getSort(List<String> sortList) {
        List<Sort.Order> orders = new ArrayList<>();

        for (String sort : sortList) {
            switch (sort) {
                case "popular" -> orders.add(Sort.Order.desc("viewCount"));
                case "priceAsc" -> orders.add(Sort.Order.asc("classPrice"));
                case "priceDesc" -> orders.add(Sort.Order.desc("classPrice"));
                case "latest" -> orders.add(Sort.Order.desc("createdAt"));
            }
        }

        return Sort.by(orders);
    }

    /**
     * 주어진 클래스 리스트에 대한 통계 정보를 계산하여 매핑(Map)으로 반환합니다.
     *
     * 반환되는 통계 정보(ClassStatisticsDto)는 클래스 ID를 키로 가지며, 다음 항목을 포함합니다:
     *  - lectureCount: 강의 수
     *  - studentCount: 수강생 수 (PaymentItem 기반)
     *  - averageRating: 평균 별점 (Review 기반)
     *
     * 이 메서드는 페이징 처리된 클래스 목록에 대해서만 호출되므로,
     * 전체 클래스가 아닌 '현재 페이지에 노출된 클래스'에 대한 통계만 계산합니다.
     * (=> 불필요한 전체 집계를 막고 성능을 최적화)
     *
     * @param classList 현재 페이지에 포함된 Classes 엔티티 목록
     * @return 클래스 ID를 키로 하는 통계 정보 맵
     */
    private Map<Long, ClassStatisticsDto> getClassStatisticsMap(List<Classes> classList) {
        // 1. 클래스 ID 목록 추출 (통계 쿼리의 대상)
        List<Long> classIds = classList.stream()
            .map(Classes::getId)
            .toList();

        // 2. 최종 반환할 통계 정보 Map (classId -> ClassStatisticsDto)
        Map<Long, ClassStatisticsDto> resultMap = new HashMap<>();

        // 3. 강의 수 조회: 각 클래스별 강의 개수를 group by 쿼리로 조회
        List<ClassesLectureCountDto> lectureCounts = lectureRepository.countLecturesByClassIds(classIds);
        for (ClassesLectureCountDto dto : lectureCounts) {
            resultMap.put(dto.getClassId(), new ClassStatisticsDto(
                dto.getClassId(),
                dto.getLectureCount(),  // 강의 수
                0L,                     // 초기 수강생 수
                0.0                     // 초기 별점
            ));
        }

        // 4. 수강생 수 조회: PaymentItem 테이블을 기반으로 각 클래스별 수강생 수 집계
        List<ClassesStudentCountDto> studentCounts = paymentItemRepository.getStudentCounts(classIds);
        for (ClassesStudentCountDto dto : studentCounts) {
            // resultMap에 기존 값이 없다면 기본값으로 넣은 뒤, 수강생 수만 업데이트
            resultMap.computeIfAbsent(dto.getClassId(), id -> new ClassStatisticsDto(id, 0L, 0L, 0.0))
                .setStudentCount(dto.getStudentCount());
        }

        // 5. 평균 별점 조회: Review 테이블 기반으로 각 클래스별 평균 별점 집계
        List<ClassesRatingAvgDto> averageRatings = reviewRepository.getAverageRatings(classIds);
        for (ClassesRatingAvgDto dto : averageRatings) {
            resultMap.computeIfAbsent(dto.getClassId(), id -> new ClassStatisticsDto(id, 0L, 0L, 0.0))
                .setAverageRating(dto.getAverageRating());
        }

        return resultMap;
    }

    // 서비스 내부 공통 통계 조회 메서드
    private Map<Long, ClassCardStatisticsDto> getClassCardStats(List<Classes> classList) {
        List<Long> classIds = classList.stream().map(Classes::getId).toList();
        Map<Long, ClassCardStatisticsDto> map = new HashMap<>();

        paymentItemRepository.getStudentStatsForCard(classIds).forEach(dto ->
                map.put(dto.getClassId(), new ClassCardStatisticsDto(dto.getClassId(), dto.getStudentCount(), 0L, 0.0, 0L))
        );

        wishlistRepository.getWishlistCounts(classIds).forEach(dto ->
                map.computeIfAbsent(dto.getClassId(), id -> new ClassCardStatisticsDto(id, 0L, 0L, 0.0, 0L))
                        .setWishlistCount(dto.getWishlistCount()));

        reviewRepository.getAvgRatings(classIds).forEach(dto -> {
            ClassCardStatisticsDto stats = map.computeIfAbsent(dto.getClassId(),
                    id -> new ClassCardStatisticsDto(id, 0L, 0L, 0.0, 0L));
            stats.setAverageRating(dto.getAverageRating());
            stats.setRatingCount(dto.getRatingCount());
        });

        return map;
    }

}
