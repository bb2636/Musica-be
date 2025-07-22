package com.example.musica_be.service.lecture;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.instrumentAnalysis.InstrumentAnalysis;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.instrumentAnalysis.AnalysisResultDto;
import com.example.musica_be.dto.instrumentAnalysis.InstrumentAnalysisRequestDto;
import com.example.musica_be.dto.instrumentAnalysis.JobCreateResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.JobStatusResponseDto;
import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.instrumentAnalysis.InstrumentAnalysisRepository;
import com.example.musica_be.repository.lecture.LectureProgressRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.instrumentAnalysis.InstrumentAnalysisService;
import com.example.musica_be.util.InstrumentCategoryMapper;
import com.example.musica_be.util.JwtUtils;
import com.example.musica_be.util.MusicAiClientImpl;
import com.example.musica_be.util.S3PresignedUrl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LectureService {
    private static final ObjectMapper objectMapper = new ObjectMapper(); // 필드로 재사용

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final ClassesRepository classesRepository;
    private final UserRepository userRepository;
    private final S3Presigner presigner;
    private final InstrumentAnalysisService instrumentAnalysisService;
    private final InstrumentAnalysisRepository instrumentAnalysisRepository;

    private final MusicAiClientImpl musicAiClientImpl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * [강의 등록 + 악기 분석 + 추천 카테고리 반환]
     * <p>
     * 강사가 클래스에 강의를 등록할 때 사용하는 메서드입니다.
     * 영상 또는 자료 중 하나 이상이 필수이며,
     * 등록 후 자동으로 Music.AI API에 분석 요청을 보내고,
     * 분석 결과를 기반으로 추천 가능한 카테고리 목록을 함께 반환합니다.
     * <p>
     * 전체 흐름:
     * 1. 강의 등록 요청에 대한 유효성 및 권한 검사
     * 2. Lecture 엔티티 생성 및 저장
     * 3. Presigned GET URL을 기반으로 악기 분석 요청 전송
     * 4. 분석 결과 조회 및 DB 저장
     * 5. 감지된 악기를 바탕으로 추천 카테고리 추출
     *
     * @param jwt     로그인한 강사의 JWT 토큰
     * @param classId 등록할 클래스의 ID
     * @param dto     강의 등록 요청 데이터
     * @return 등록된 강의 ID 및 추천 카테고리 리스트가 포함된 응답 DTO
     */
    @Transactional
    public LectureCreateResDto createLecture(String jwt, Long classId, LectureCreateReqDto dto) {
        // 1. 유효성 체크
        if ((dto.getVideoUrl() == null || dto.getVideoUrl().isBlank()) &&
            (dto.getFileUrl() == null || dto.getFileUrl().isBlank())) {
            throw new IllegalArgumentException("강의 영상 또는 자료는 하나 이상 필요합니다.");
        }

        // 🔍 2. S3 객체 키 추출
        String videoObjectKey = dto.getVideoObjectKey();
        String fileObjectKey = dto.getFileObjectKey();
//        String videoObjectKey = extractVideoObjectKey(dto.getVideoUrl());
//        String fileObjectKey = extractVideoObjectKey(dto.getFileUrl());

        // 3. 사용자 권한 검사
        Long userId = JwtUtils.extractUserId(jwt);
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("클래스가 존재하지 않습니다."));
        validateInstructor(classes, userId);

        // 4. 강의 엔티티 저장
        Lecture lecture = Lecture.builder()
            .classes(classes)
            .title(dto.getTitle())
            .videoUrl(dto.getVideoUrl())
            .videoObjectKey(videoObjectKey)
            .fileUrl(dto.getFileUrl())
            .fileObjectKey(fileObjectKey)
            .lectureOrder(dto.getLectureOrder())
            .duration(dto.getDuration())
            .build();
        lectureRepository.save(lecture);

        // 5. 추천 카테고리 분석
        List<String> recommendedCategories = List.of();

        if (videoObjectKey != null && videoObjectKey.endsWith(".mp4")) {
            try {
                String s3DownloadUrl = generatePresignedDownloadUrl(videoObjectKey);
                log.info("🎯 MusicAI 요청 URL: {}", s3DownloadUrl);

                InstrumentAnalysisRequestDto analysisRequestDto =
                    new InstrumentAnalysisRequestDto(lecture.getId(), s3DownloadUrl);

                JobCreateResponseDto response = instrumentAnalysisService.requestAnalysis(analysisRequestDto);
                String jobId = response.getId();

                InstrumentAnalysis analysis = instrumentAnalysisRepository.findByJobId(jobId)
                    .orElseThrow(() -> new IllegalStateException("jobId로 분석 정보를 찾을 수 없습니다."));

                AnalysisResultDto resultDto = instrumentAnalysisService.waitAndParse(analysis);
                List<String> detectedInstruments = resultDto.getDetectedInstruments();
                recommendedCategories = InstrumentCategoryMapper.toCategories(detectedInstruments);
            } catch (Exception e) {
                log.error("🎯 MusicAI 분석 실패: {}", e.getMessage(), e);
                // 실패하더라도 서비스는 계속 진행
            }
        } else if (dto.getVideoUrl() != null) {
            log.warn("⚠️ videoUrl이 존재하지만 videoObjectKey가 없거나 mp4가 아님 → MusicAI 분석 생략: {}", dto.getVideoUrl());
        }

        // 6. 응답 반환
        return LectureCreateResDto.builder()
            .lectureId(lecture.getId())
            .recommendedCategories(recommendedCategories)
            .build();
    }

    // 강의 수정
    @Transactional
    public void updateLecture(String jwt, Long lectureId, LectureUpdateReqDto dto) {
        Long userId = JwtUtils.extractUserId(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        validateInstructor(lecture.getClasses(), userId);

        if ((dto.getVideoUrl() == null || dto.getVideoUrl().isBlank()) &&
            (dto.getFileUrl() == null || dto.getFileUrl().isBlank())) {
            throw new IllegalArgumentException("강의 영상 또는 자료 중 하나는 필수입니다.");
        }

        boolean videoChanged = !Objects.equals(lecture.getVideoUrl(), dto.getVideoUrl());

        String videoObjectKey = extractVideoObjectKey(dto.getVideoUrl());
        String fileObjectKey = extractVideoObjectKey(dto.getFileUrl());

        lecture.update(
            dto.getTitle(),
            dto.getVideoUrl(),
            dto.getFileUrl(),
            dto.getLectureOrder(),
            videoObjectKey,
            fileObjectKey,
            dto.getDuration()
        );

        if (videoChanged && dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            // 기존 분석 삭제
            instrumentAnalysisRepository.deleteByLectureId(lecture.getId());

            // 새 presigned URL로 분석 요청
            String s3DownloadUrl = generatePresignedDownloadUrl(videoObjectKey);
            JobCreateResponseDto response = instrumentAnalysisService.requestAnalysis(
                new InstrumentAnalysisRequestDto(lecture.getId(), s3DownloadUrl)
            );

            // 결과 기다리고 파싱
            InstrumentAnalysis analysis = instrumentAnalysisRepository.findByJobId(response.getId())
                .orElseThrow(() -> new IllegalStateException("분석 정보 없음"));
            instrumentAnalysisService.waitAndParse(analysis);
        }
    }

    // 강의 삭제
    @Transactional
    public void deleteLecture(String jwt, Long lectureId) {
        Long userId = JwtUtils.extractUserId(jwt);

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        validateInstructor(lecture.getClasses(), userId);

        // ✅ 연관 분석 데이터 먼저 삭제 + flush
        instrumentAnalysisRepository.deleteByLectureId(lectureId);
        instrumentAnalysisRepository.flush(); // ← 이걸 꼭 추가해줘

        // ✅ 강의 삭제
        lectureRepository.delete(lecture);
    }

    // 강의 상세 조회
    @Transactional(readOnly = true)
    public LectureDetailResDto getLectureDetail(String jwt, Long lectureId) {
        // 1. 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 2. Presigned URL 생성
        String videoUrl = null;
        if (lecture.getVideoObjectKey() != null && !lecture.getVideoObjectKey().isBlank()) {
            videoUrl = generatePresignedDownloadUrl(lecture.getVideoObjectKey());
        }
        String fileUrl = null;
        if (lecture.getFileObjectKey() != null && !lecture.getFileObjectKey().isBlank()) {
            fileUrl = generatePresignedDownloadUrl(lecture.getFileObjectKey());
        }

        // ✅ 3. 분석 결과 조회
        InstrumentAnalysis analysis = instrumentAnalysisRepository.findByLecture(lecture)
            .orElse(null);

        // 4. jwt가 없거나 빈 문자열이면 비로그인 사용자 처리 → 시청 기록 없음
        if (jwt == null || jwt.isBlank()) {
            return LectureDetailResDto.from(lecture, null, videoUrl, fileUrl, analysis);
        }

        // 5. 사용자 조회
        Long userId = JwtUtils.extractUserId(jwt);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 6. 시청 진행률 조회
        LectureProgress progress = lectureProgressRepository
            .findByUserAndLecture(user, lecture)
            .orElse(null);

        // 7. DTO로 변환
        return LectureDetailResDto.from(lecture, progress, videoUrl, fileUrl, analysis);
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
        // - 내부적으로 시청 완료 여부(isCompleted)도 자동 판단됨 (LectureProgress 도메인 메서드)
        progress.updateProgress(dto.getWatchedSeconds());

        // 6. 변경사항 저장 (기존이면 update, 신규면 insert)
        lectureProgressRepository.save(progress);
    }

    // 강의 목록 조회
    // 1. 공개용 - 진행률 없이 기본 정보만 반환
    @Transactional(readOnly = true)
    public List<LectureSummaryDto> getLectureList(Long classId) {
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
//    @Transactional(readOnly = true)
//    public LectureWatchResDto watchLecture(String jwt, Long lectureId) {
//        // 유저 인증 여부는 단순 확인용 (특별한 로직 없이)
//        // todo: 유저 인증 구현
//        Long userId = JwtUtils.extractUserId(jwt);
//
//        // 존재하는 강의인지 확인
//        Lecture lecture = lectureRepository.findById(lectureId)
//            .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
//
//        // lecture 에서 videoObjectKey 를 받아와 S3 Presigned Download URL 요청
//        // 응답으로 온 downloadUrl 이 실제로 영상을 볼 수 있는 url
//        String videoDownloadUrl = null;
//        if (lecture.getVideoObjectKey() != null && !lecture.getVideoObjectKey().isBlank()) {
//            videoDownloadUrl = generateDownloadUrl(lecture.getVideoObjectKey());
//        }
//        String fileDownloadUrl = null;
//        if (lecture.getFileObjectKey() != null && !lecture.getFileObjectKey().isBlank()) {
//            fileDownloadUrl = generateDownloadUrl(lecture.getFileObjectKey());
//        }
//
//        return LectureWatchResDto.from(lecture, videoDownloadUrl, fileDownloadUrl);
//    }

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

    // S3 Presigned URL 생성 (통합 메서드)
    // 강사가 강의 추가 페이지에서 등록 버튼을 클릭했을 때 실행되는 로직

    /**
     * 강의 등록 시 사용하는 통합 Presigned URL 생성 메서드
     * <p>
     * 프론트에서 강의 등록 버튼을 클릭했을 때, 강의 영상(mp4)과 부가 자료(pdf, jpg 등)에 대해
     * S3에 업로드 가능한 Presigned URL을 생성하여 응답한다.
     * <p>
     * Presigned URL이란?
     * - 클라이언트가 직접 S3에 파일을 업로드할 수 있도록 일정 시간 동안 유효한 URL을 생성해 주는 기능이다.
     * - 일반적으로 서버가 업로드용 URL을 생성하여 프론트에 전달하고, 프론트는 해당 URL을 통해 파일을 직접 업로드함.
     *
     * @param videoName 업로드할 영상 파일명 (예: lecture.mp4) – null 또는 빈 문자열일 수 있음
     * @param fileName  업로드할 일반 파일명 (예: sheet.pdf, image.png 등) – null 또는 빈 문자열일 수 있음
     * @return 업로드 URL과 S3 객체 URL이 담긴 Map
     * 예시:
     * {
     * "videoUploadUrl": "...",
     * "videoUrl": "...",
     * "fileUploadUrl": "...",
     * "fileUrl": "..."
     * }
     */
    public Map<String, String> generatePresignedUploadUrls(String videoName, String fileName) {
        Map<String, String> result = new java.util.HashMap<>();
        // 강의 영상인 경우 (mp4 만 허용)
        if (videoName != null && !videoName.isBlank()) {
            String extension = "";

            int dotIndex = videoName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = videoName.substring(dotIndex); // ".mp4"
            }

            String videoKey = "lectures/" + UUID.randomUUID() + extension;

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
        // 영상 외 자료인 경우 (pdf, jpg, png 만 허용)
        if (fileName != null && !fileName.isBlank()) {
            String extension = "";

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex); // ".pdf"
            }

            String fileKey = "lectures/" + UUID.randomUUID() + extension;
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

    /**
     * 강의 등록 또는 수정 시, 해당 강의의 소유자(강사)가 요청한 것인지 확인하는 권한 검사 메서드
     *
     * @param classes 확인 대상 클래스 엔티티
     * @param userId  요청한 사용자 ID
     * @throws SecurityException 강사가 아닌 경우 예외 발생
     */
    private void validateInstructor(Classes classes, Long userId) {
        if (!classes.getInstructor().getId().equals(userId)) {
            throw new SecurityException("권한이 없습니다.");  // 강사 본인이 아닐 경우 거부
        }
    }

    /**
     * S3 객체 URL 에서 객체 키(object key)를 추출하는 메서드
     * <p>
     * 예: https://bucket.s3.amazonaws.com/lectures/abc.mp4?... → lectures/abc.mp4 추출
     *
     * @param url S3 Presigned upload URL
     * @return S3 내부 객체 경로 (object key)
     * @throws IllegalArgumentException 잘못된 URL 형식이거나 null/빈 문자열일 경우
     */
    private String extractVideoObjectKey(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // "/lectures/abc.mp4" 형태

            // 디버깅 로그
            log.info("🎯 [extractVideoObjectKey] 원본 URL: {}", url);
            log.info("🎯 [extractVideoObjectKey] URI path: {}", path);

            // 슬래시 방어 처리
            String objectKey = path.startsWith("/") ? path.substring(1) : path;

            log.info("🎯 [extractVideoObjectKey] 최종 objectKey: {}", objectKey);

            return objectKey;
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다: " + url);
        }
    }

    /**
     * 강의 영상 또는 파일의 S3 Presigned 다운로드 URL을 생성하는 메서드
     * <p>
     * 프론트는 이 URL을 통해 실제 파일을 재생하거나 다운로드할 수 있음
     *
     * @param key S3에 저장된 객체 키 (예: lectures/abc.mp4)
     * @return 시간 제한이 있는 Presigned 다운로드 URL
     * @throws IllegalArgumentException 객체 키가 비어있거나 null일 경우
     */
    public String generatePresignedDownloadUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("S3 객체 키(key)는 null이거나 비어 있을 수 없습니다.");
        }

        // ✅ key가 "/lectures/abc.mp4"처럼 슬래시로 시작하면 제거
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        return S3PresignedUrl.generateDownloadUrl(
            presigner,
            bucket,
            key,
            Duration.ofMinutes(60)
        );
    }

    /**
     * 파일 확장자에 따라 Content-Type을 유추하는 메서드
     * <p>
     * Presigned URL 생성 시 올바른 Content-Type을 설정하기 위함
     *
     * @param filename 파일명 (확장자 포함)
     * @return 해당 파일의 MIME 타입
     * @throws IllegalArgumentException 지원하지 않는 확장자일 경우
     */
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
     *
     * @param watchedSeconds 현재까지 시청한 시간(초)
     * @param totalDuration  전체 강의 시간(초)
     * @return 진행률 퍼센트 (0~100 사이 정수)
     */
    private int calculateProgressPercent(int watchedSeconds, int totalDuration) {
        if (totalDuration <= 0) return 0;
        return Math.min(100, (int) ((watchedSeconds / (double) totalDuration) * 100));
    }

}
