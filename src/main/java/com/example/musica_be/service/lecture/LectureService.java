package com.example.musica_be.service.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureProgressRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import com.example.musica_be.util.S3PresignedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final ClassesRepository classesRepository;
    private final UserRepository userRepository;
    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 강의 등록
    @Transactional
    public Long createLecture(String jwt, Long classId, LectureCreateReqDto dto) {
        // 먼저 영상 또는 파일 중 하나는 필수라는 유효성 검사 진행
        if ((dto.getVideoUrl() == null || dto.getVideoUrl().isBlank()) &&
            (dto.getFileUrl() == null || dto.getFileUrl().isBlank())) {
            throw new IllegalArgumentException("강의 영상(videoUrl) 또는 강의 자료(fileUrl) 중 하나는 반드시 포함되어야 합니다.");
        }

        // 사용자 아이디 추출 (userId)
        Long userId = JwtUtils.extractUserId(jwt);

        // 클래스 존재 여부 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));
        // 강의 등록 권한 확인 (해당 클래스의 강사인지)
        validateInstructor(classes, userId);
        // videoObjectKey 추출
        // videoObjectKey 로 나중에 Presigned GET URL 생성 가능
        String videoUrl = dto.getVideoUrl(); // ex: https://musica-test-bk.s3.amazonaws.com/lectures/uuid_sample.mp4
        String videoObjectKey = extractVideoObjectKey(videoUrl);
        // fileObjectKey 추출
        String fileUrl = dto.getFileUrl(); // ex: https://musica-test-bk.s3.amazonaws.com/lectures/uuid_sample.pdf
        String fileObjectKey = extractVideoObjectKey(fileUrl);

        // 강의 엔티티 생성
        Lecture lecture = Lecture.builder()
            .classes(classes)
            .title(dto.getTitle())
            .videoUrl(dto.getVideoUrl()) // 프론트에서 전달받은 S3에 저장한 객체의 URL - 이 URL 로는 권한 문제로 시청 불가 (403)
            .videoObjectKey(videoObjectKey) // Presigned GET URL 생성할 때 필요한 key 값 (영상 보여줄 때 사용)
            .fileUrl(dto.getFileUrl()) // 프론트에서 전달받은 S3에 저장한 객체의 URL - 이 URL 로는 권한 문제로 파일 열람 불가 (403)
            .fileObjectKey(fileObjectKey) // Presigned GET URL 생성할 때 필요한 key 값 (파일 보여줄 때 사용)
            .lectureOrder(dto.getLectureOrder())
            .build();

        // DB 저장 및 ID 반환
        return lectureRepository.save(lecture).getId();
    }

    // 강의 수정
    @Transactional
    public void updateLecture(String jwt, Long lectureId, LectureUpdateReqDto dto) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 강의인지 확인
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        // 유저 권한 확인
        validateInstructor(lecture.getClasses(), userId);

        lecture.update(
            dto.getTitle(),
            dto.getVideoUrl(),
            dto.getSheetMusicUrl(),
            dto.getLectureOrder()
        );
    }

    // 강의 삭제
    @Transactional
    public void deleteLecture(String jwt, Long lectureId) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 강의인지 확인
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
        // 유저 권한 확인
        validateInstructor(lecture.getClasses(), userId);

        lectureRepository.delete(lecture);
    }

    // 강의 상세 조회
    @Transactional(readOnly = true)
    public LectureDetailResDto getLectureDetail(String jwt, Long lectureId) {
        // 1. 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 2. JWT가 null 또는 빈 문자열인 경우 → 비로그인 사용자로 처리
        if (jwt == null || jwt.isBlank()) {
            return LectureDetailResDto.from(lecture, null);  // 진행률 없음
        }

        // 3. 로그인 사용자일 경우 → userId 추출
        Long userId;
        try {
            userId = JwtUtils.extractUserId(jwt);
        } catch (Exception e) {
            return LectureDetailResDto.from(lecture, null); // JWT 파싱 실패 → 비로그인 처리
        }

        // 4. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 5. 시청 진행률 조회
        LectureProgress progress = lectureProgressRepository
            .findByUserAndLecture(user, lecture)
            .orElse(null);

        // 6. DTO 변환
        return LectureDetailResDto.from(lecture, progress);
    }

    // 강의 목록 조회
    // 1. 공개용 - 진행률 없이 기본 정보만 반환
    @Transactional(readOnly = true)
    public List<LectureSummaryDto> getPublicLectureList(Long classId) {
        // 존재하는 클래스인지 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));
        // 강의 목록 조회
        List<Lecture> lectures = lectureRepository.findByClasses(classes);

        return lectures.stream()
            .sorted(Comparator.comparingInt(Lecture::getLectureOrder)) // 강의 아이디 순서대로 정렬
            .map(LectureSummaryDto::from) // 진행률 없이 기본 정보만
            .collect(Collectors.toList()); // 리스트로 반환
    }

    // 2. 수강생용 - 진행률 포함
    @Transactional(readOnly = true)
    public List<LectureSummaryDto> getLectureListForStudent(Long classId, Long userId) {
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Lecture> lectures = lectureRepository.findByClasses(classes);

        return lectures.stream()
            .sorted(Comparator.comparingInt(Lecture::getLectureOrder))
            .map(lecture -> {
                // 개인별 시청률 가져오기
                LectureProgress progress = lectureProgressRepository
                    .findByUserAndLecture(user, lecture)
                    .orElse(null);

                return LectureSummaryDto.from(lecture, progress);
            })
            .collect(Collectors.toList());
    }

    // 3. 강사용 - 본인이 올린 강의만
    @Transactional(readOnly = true)
    public List<LectureSummaryDto> getLectureListForInstructor(Long classId, Long userId) {
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("해당 클래스의 강사가 아닙니다.");
        }

        List<Lecture> lectures = lectureRepository.findByClasses(classes);

        return lectures.stream()
            .sorted(Comparator.comparingInt(Lecture::getLectureOrder))
            .map(LectureSummaryDto::from) // 진행률 없음
            .collect(Collectors.toList());
    }

    // 강의 시청 (간단히 videoUrl 과 진행률만 응답)
    // videoUrl - S3 Presigned Download URL
    @Transactional(readOnly = true)
    public LectureWatchResDto watchLecture(String jwt, Long lectureId) {
        // 유저 인증 여부는 단순 확인용 (특별한 로직 없이)
        // todo: 유저 인증 구현
        Long userId = JwtUtils.extractUserId(jwt);

        // 존재하는 강의인지 확인
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // lecture 에서 videoObjectKey 를 받아와 S3 Presigned Download URL 요청
        // 응답으로 온 downloadUrl 이 실제로 영상을 볼 수 있는 url
        String videoDownloadUrl = null;
        if (lecture.getVideoObjectKey() != null && !lecture.getVideoObjectKey().isBlank()) {
            videoDownloadUrl = generateDownloadUrl(lecture.getVideoObjectKey());
        }

        String fileDownloadUrl = null;
        if (lecture.getFileObjectKey() != null && !lecture.getFileObjectKey().isBlank()) {
            fileDownloadUrl = generateDownloadUrl(lecture.getFileObjectKey());
        }

        return LectureWatchResDto.from(lecture, videoDownloadUrl, fileDownloadUrl);
    }

    // 강의 순서 변경
    @Transactional
    public List<Long> updateLectureOrder(String jwt, Long classId, LectureOrderUpdateReqDto dto) {
        // 1. JWT 토큰에서 사용자 ID 추출
        Long userId = JwtUtils.extractUserId(jwt);

        // 2. 해당 클래스가 존재하는지 검증
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        // 3. 현재 사용자가 이 클래스의 강사인지 확인 (권한 검증)
        validateInstructor(classes, userId);

        // 4. 순서(order)의 중복 여부 및 유효성 검사용 Set
        Set<Integer> seenOrders = new HashSet<>();

        List<Long> updatedLectureIds = new ArrayList<>();

        // 5. 순서 변경 요청 목록을 순회하며 하나씩 처리
        for (LectureOrderUpdateReqDto.LectureOrderDto item : dto.getOrders()) {

            // 5-1. 동일한 순서가 여러 강의에 지정되어 있는 경우 에러
            if (!seenOrders.add(item.getOrder())) {
                throw new IllegalArgumentException("중복된 순서(order)가 있습니다.");
            }

            // 5-2. 순서는 1 이상 양수만 허용
            if (item.getOrder() <= 0) {
                throw new IllegalArgumentException("순서는 1 이상의 양수여야 합니다.");
            }

            // 5-3. 해당 강의 ID가 실제 존재하는지 확인
            Lecture lecture = lectureRepository.findById(item.getLectureId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

            // 5-4. 해당 강의가 지정된 클래스에 속한 강의인지 확인
            if (!lecture.getClasses().getId().equals(classId)) {
                throw new IllegalArgumentException("강의가 해당 클래스에 속하지 않습니다.");
            }

            // 5-5. 순서 변경 적용
            lecture.changeOrder(item.getOrder());
            updatedLectureIds.add(lecture.getId());
        }

        return updatedLectureIds;
    }

    // 특정 사용자의 특정 강의에 대한 시청 시간을 저장하거나 갱신하는 로직
    @Transactional
    public void saveProgress(String jwt, Long lectureId, LectureProgressSaveReqDto dto) {
        // 1. JWT 토큰에서 사용자 ID 추출
        Long userId = JwtUtils.extractUserId(jwt);

        // 2. 강의 ID로 강의 정보 조회 (없으면 예외 발생)
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 3. 사용자 ID로 사용자 정보 조회 (없으면 예외 발생)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 기존 시청 기록 조회 (LectureProgress)
        // - 이미 시청 이력이 있다면 가져오고
        // - 없으면 새로운 객체 생성 (초기값: watchedSeconds=0, isCompleted=false)
        LectureProgress progress = lectureProgressRepository
            .findByUserAndLecture(user, lecture)
            .orElse(LectureProgress.builder()
                .user(user)
                .lecture(lecture)
                .watchedSeconds(0)
                .isCompleted(false)
                .build()
            );

        // 5. 시청 시간(watchedSeconds) 업데이트
        // - 내부적으로 시청 완료 여부(isCompleted)도 자동 판단됨
        progress.updateProgress(dto.getWatchedSeconds());

        // 6. 변경사항 저장 (기존이면 update, 신규면 insert)
        lectureProgressRepository.save(progress);
    }

    // S3 Presigned URL 생성 (통합 메서드)
    public Map<String, String> generatePresignedUploadUrls(String videoName, String fileName) {
        Map<String, String> result = new java.util.HashMap<>();
        // 강의 영상인 경우 (mp4)
        if (videoName != null && !videoName.isBlank()) {
            String videoKey = "lectures/" + UUID.randomUUID() + "_" + videoName;
            // 파일명에서 타입 추출
            String videoContentType = guessContentType(videoName);

            // 영상용 uploadUrl 요청
            String videoUploadUrl = S3PresignedUrl.generateUploadUrl(
                presigner, bucket, videoKey, videoContentType, Duration.ofMinutes(60)
            );

            // s3에 저장된 영상 객체의 url (권한 문제로 접근 불가 - 403)
            // 403 문제를 해결하기 위해 다운로드용 presigned url 을 발급받아야 하는데 그 때 사용되는 url 임
            String videoUrl = "https://" + bucket + ".s3.amazonaws.com/" + videoKey;

            result.put("videoUploadUrl", videoUploadUrl);
            result.put("videoUrl", videoUrl);
        }
        // 영상 외 자료인 경우 (pdf, jpg, png ... )
        if (fileName != null && !fileName.isBlank()) {
            String fileKey = "lectures/" + UUID.randomUUID() + "_" + fileName;
            // 파일명에서 타입 추출
            String fileContentType = guessContentType(fileName);

            // 영상 외 자료용 uploadUrl 요청
            String fileUploadUrl = S3PresignedUrl.generateUploadUrl(
                presigner, bucket, fileKey, fileContentType, Duration.ofMinutes(60)
            );

            // s3에 저장된 영상 외 자료 객체의 url (권한 문제로 접근 불가 - 403)
            // 403 문제를 해결하기 위해 다운로드용 presigned url 을 발급받아야 하는데 그 때 사용되는 url 임
            String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + fileKey;

            result.put("fileUploadUrl", fileUploadUrl);
            result.put("fileUrl", fileUrl);
        }

        return result;
    }

    // ====== 헬퍼 메서드 ======
    // 유저가 강사인지 확인하는 권한 확인 메서드
    private void validateInstructor(Classes classes, Long userId) {
        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("권한이 없습니다.");
        }
    }

    // 받아온 S3 Presigned uploadUrl 에서 key 를 추출하는 메서드
    private String extractVideoObjectKey(String url) {
        if (url == null || url.isBlank()) {
            return null; // 혹은 throw new IllegalArgumentException("URL이 제공되지 않았습니다.");
        }
        try {
            URI uri = new URI(url);
            return uri.getPath().substring(1); // "/lectures/..." → "lectures/..."
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다: " + url);
        }
    }

    // 강의 다운로드 URL 생성 - S3 Presigned
    // String key - fileObjectKey (fileUrl 에서 추출한 값, DB에 저장되어 있는 값)
    // 응답으로 downloadUrl, 즉 실제로 강의 영상을 볼 수 있는 url 을 반환
    // todo: api 테스트 후 public → private 으로 변경
    public String generateDownloadUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("S3 객체 키(key)는 null이거나 비어 있을 수 없습니다.");
        }

        return S3PresignedUrl.generateDownloadUrl(
            presigner,
            bucket,
            key,
            Duration.ofMinutes(60)
        );
    }

    // 파일 이름을 받아 확장자를 보고 content-type 을 자동으로 설정해주는 메서드
    private static String guessContentType(String filename) {
        if (filename == null) throw new IllegalArgumentException("파일명이 비어 있습니다.");

        filename = filename.toLowerCase();

        if (filename.endsWith(".pdf")) return "application/pdf";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".mp4")) return "video/mp4";

        throw new IllegalArgumentException("허용되지 않은 파일 형식입니다: " + filename);
    }

    /**
     * 시청 시간 기반 진행률 퍼센트를 계산하는 메서드
     * @param watchedSeconds 현재까지 시청한 시간(초)
     * @param totalDuration 전체 강의 시간(초)
     * @return 진행률 퍼센트 (0~100 사이 정수)
     */
    private int calculateProgressPercent(int watchedSeconds, int totalDuration) {
        if (totalDuration <= 0) return 0;
        return Math.min(100, (int)((watchedSeconds / (double) totalDuration) * 100));
    }

}
