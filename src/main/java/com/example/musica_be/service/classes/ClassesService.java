package com.example.musica_be.service.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.*;
import com.example.musica_be.repository.classes.CategoryRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.repository.wishlist.WishlistRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final WishlistRepository wishlistRepository;

    // 클래스 등록
    @Transactional
    public Long createClass(ClassCreateReqDto dto, String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);
        log.info("이게 출력된다면 클래스 서비스까지 들어온 것: ClassService");
        System.out.println("이게 출력된다면 클래스 서비스까지 들어온 것: ClassService");

        // 존재하는 사용자인지 확인
        User instructor = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
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
            .isRecommended(dto.getIsRecommended() != null && dto.getIsRecommended())
            .build();

        return classesRepository.save(classes).getId();
    }

    // 클래스 수정
    @Transactional
    public void updateClass(Long classId, ClassUpdateReqDto dto, String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 클래스인지 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));
        // 유저 권한 확인
        validateInstructor(classes, userId);
        // 존재하는 난이도인지 확인
        Level difficulty = levelRepository.findById(dto.getDifficultyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 난이도 ID입니다."));
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
            dto.getClassPrice(),
            dto.getIsRecommended() != null && dto.getIsRecommended()
        );
    }

    // 클래스 삭제
    @Transactional
    public void deleteClass(Long classId, String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 클래스인지 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));
        // 유저 권한 확인
        validateInstructor(classes, userId);

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

    // ====== 헬퍼 메서드 ======
    private void validateInstructor(Classes classes, Long userId) {
        log.info("클래스의 유저 아이디: {}", classes.getInstructor().getId());
        log.info("파라미터 유저 아이디: {}", userId);
        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("권한이 없습니다: 강사 본인의 클래스만 수정/삭제할 수 있습니다.");
        }
    }

    // 추천 클래스 조회 (1순위 관리자지정, 2순위 최신순)
    @Transactional(readOnly = true)
    public List<ClassCardDto> getRecommendedClasses(String jwt) {
        Long userId = null;
        // [선택적 인증] JWT가 있을 때만 추출
        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                userId = JwtUtils.extractUserId(jwt);
                log.info("추천 클래스 요청 유저 ID: {}", userId);
            } catch (Exception e) {
                log.warn("JWT 파싱 실패 – 비회원 접근으로 처리");
            }
        }

        List<ClassCardDto> result = new ArrayList<>();

        // 1. 관리자가 추천한 클래스 최대 4개까지 조회
        List<Classes> recommended = classesRepository.findTop4ByIsRecommendedTrueOrderByCreatedAtDesc();

        int needed = 4;

        if (recommended != null && !recommended.isEmpty()) {
            for (Classes cls : recommended) {
                double rating = calculateAvgRating(cls.getId());
                result.add(ClassCardDto.from(cls, rating));
            }
            needed -= recommended.size();
        }

        // 2. 추천 클래스가 4개보다 부족하면 최신순에서 추가로 채움
        if (needed > 0) {
            List<Classes> latest = classesRepository.findByIsRecommendedFalseOrderByCreatedAtDesc();

            for (Classes cls : latest) {
                if (needed == 0) break;

                // 이미 추천에 들어간 클래스 중복 방지 (옵션)
                if (recommended.contains(cls)) continue;

                double rating = calculateAvgRating(cls.getId());
                result.add(ClassCardDto.from(cls, rating));
                needed--;
            }
        }

        return result;
    }
    // 클래스별 평균 별점 구하는 로직 - (FE) ClassCard 표시용
    private double calculateAvgRating(Long classId) {
        return reviewRepository.calculateAverageRatingByClassId(classId).orElse(0.0);
    }

    // Popularity 점수 기반 인기 클래스 조회
    @Transactional(readOnly = true)
    public List<ClassCardDto> getPopularClasses() {
        List<Classes> allClasses = classesRepository.findAll();

        List<ClassPopularityDto> scoredList = allClasses.stream().map(classes -> {
            int orderCount = paymentItemRepository.countByClasses(classes); // 결제 수
            int wishlistCount = wishlistRepository.countByClasses(classes); // 찜 수
            int score = (orderCount * 2) + wishlistCount;

            return new ClassPopularityDto(classes, score);
        }).collect(Collectors.toList());

        // 점수 내림차순, 같으면 createdAt 내림차순
        scoredList.sort((a, b) -> {
            int cmp = Integer.compare(b.getScore(), a.getScore());
            if(cmp == 0) {
                return b.getClasses().getCreatedAt().compareTo(a.getClasses().getCreatedAt());
            }
            return cmp;
        });

        // 별점 포함 ClassCardDto로 변환, 상위 16개
        return scoredList.stream()
                .limit(16)
                .map(dto -> {
                    Classes cls = dto.getClasses();
                    double rating = calculateAvgRating(cls.getId());
                    return ClassCardDto.from(cls, rating);
                })
                .collect(Collectors.toList());
    }

    // 최신 클래스 (16개 limit)
    @Transactional(readOnly = true)
    public List<ClassCardDto> getLatestClasses() {
        List<Classes> latestClasses = classesRepository.findTop16ByOrderByCreatedAtDesc();

        return latestClasses.stream()
                .map(cls -> {
                    double rating = calculateAvgRating(cls.getId());
                    return ClassCardDto.from(cls, rating);
                })
                .collect(Collectors.toList());
    }

}
