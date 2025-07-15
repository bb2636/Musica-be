package com.example.musica_be.service.instrumentAnalysis;

import com.example.musica_be.domain.instrumentAnalysis.*;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.dto.instrumentAnalysis.*;
import com.example.musica_be.repository.instrumentAnalysis.MusicAiClient;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.instrumentAnalysis.InstrumentAnalysisRepository;
import com.example.musica_be.util.S3PresignedUrl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentAnalysisService {
    private static final ObjectMapper objectMapper = new ObjectMapper(); // 필드로 재사용

    private final InstrumentAnalysisRepository analysisRepository;
    private final LectureRepository lectureRepository;
    private final MusicAiClient musicAiClient;
    private final S3Presigner s3Presigner;

    @Value("${musicai.api.workflow-slug}")
    private String workflowSlug;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

//    /**
//     * [1] S3 Presigned Upload URL 발급
//     * - 강사가 영상(mp4) 또는 자료(pdf 등)을 업로드하기 전에 호출됨
//     * - 업로드용 URL과 일반 S3 객체 URL을 함께 반환
//     */
//    public UrlResponseDto getUrl() {
//        String videoFileName = "video_" + UUID.randomUUID() + ".mp4";
//        String videoKey = "lectures/" + videoFileName;
//
//        String videoUploadUrl = S3PresignedUrl.generateUploadUrl(
//            s3Presigner, bucket, videoKey, "video/mp4", Duration.ofMinutes(60)
//        );
//        String videoUrl = "https://" + bucket + ".s3.amazonaws.com/" + videoKey;
//
//        return new UrlResponseDto(videoUploadUrl, videoUrl);
//    }
//
//    /**
//     * [2] 분석 Job 생성 - 사용자가 직접 요청한 경우
//     * - 프론트에서 S3 업로드 완료 후 download URL 을 전달하여 분석 요청
//     */
//    public JobCreateResponseDto createJob(JobCreateRequestDto request) {
//        return musicAiClient.createJob(request);
//    }
//
//    /**
//     * [3] 분석 결과 조회 (jobId 기반)
//     */
//    public InstrumentAnalysisResponseDto getResult(String jobId) {
//        InstrumentAnalysis analysis = analysisRepository.findByJobId(jobId)
//            .orElseThrow(() -> new EntityNotFoundException("Job not found with id = " + jobId));
//        return InstrumentAnalysisResponseDto.from(analysis);
//    }

    /**
     * 통합 요청 (강의 조회 + 분석 Job 생성 + 결과 저장)
     * 강의 등록 후 자동으로 MusicAI에 분석 요청을 보내고, 결과는 DB에 PENDING 상태로 저장
     * S3에서 다운로드 가능한 Presigned URL 필요
     */
    @Transactional
    public JobCreateResponseDto requestAnalysis(InstrumentAnalysisRequestDto request) {
        // 1. 강의 존재 여부 확인
        Lecture lecture = lectureRepository.findById(request.getLectureId())
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의 아이디입니다. => " + request.getLectureId()));

        // 2. Music.AI 분석 요청 (workflowSlug 는 설정파일에서 로드됨)
        String jobName = "job_" + UUID.randomUUID(); // 고유 Job ID 생성
        JobCreateRequestDto.JobParams params = new JobCreateRequestDto.JobParams(request.getS3DownloadUrl());
        JobCreateRequestDto jobRequest = new JobCreateRequestDto(jobName, workflowSlug, params);
        JobCreateResponseDto jobResponse = musicAiClient.createJob(jobRequest);
        // ✅ music.ai 에서 분석 요청은 잘 들어가는 것을 확인

        // 분석 요청 결과 로그 확인
        log.info("분석 결과 jod 아이디: {}", jobResponse);

        // 3. 분석 결과 저장 (초기 상태는 PENDING)
        // 분석이 연결된 강의 저장 (참조관계 - ManyToOne)
        // music.ai 에서 생성된 job 아이디 값 저장
        // 분석 되상이 되는 Presigned s3 video download url 저장
        // status 는 PENDING 으로 저장
        // 분석 요청 시간 저장
        InstrumentAnalysis analysis = InstrumentAnalysis.builder()
            .lecture(lecture)
            .jobId(jobResponse.getId())
            .videoUrl(request.getS3DownloadUrl())
            .status(AnalysisStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();
        analysisRepository.save(analysis);

        return jobResponse; // job 아이디를 가지고 있는 dto (예시: {"id":"a913v240-7ce1-3571-a22q-f4q3cdabcd1e"})
    }

    /**
     * 분석 결과 갱신 및 추출
     * Music.AI 분석 Job 결과를 조회하고, 분석이 완료되었을 경우 결과를 DB에 저장하며,
     * 감지된 악기 목록만 클라이언트에 반환하는 메서드.
     *
     * 전체 흐름:
     * 1. Music.AI API로부터 Job 결과 조회
     * 2. 분석 상태 확인 (SUCCEEDED가 아닐 경우 예외 처리)
     * 3. 결과 JSON 문자열 → 내부 DTO로 파싱
     * 4. 분석 결과(instruments, probabilities, thresholds)를 DB에 저장
     * 5. 감지된 악기 목록(값이 true인 항목들)만 리스트로 추출하여 반환
     */
    public AnalysisResultDto updateAnalysisResult(String jobId) {
        // 1. Music.AI API로부터 Job 결과를 가져옴
        JobStatusResponseDto jobStatus = musicAiClient.getJobResult(jobId);

        // 2. 분석 상태 확인 (SUCCEEDED 외에는 아직 분석이 완료되지 않은 상태)
        // music.ai api 응답 중 status 에 해당하는 값을 확인
        if (!"SUCCEEDED".equalsIgnoreCase(jobStatus.getStatus())) {
            throw new IllegalStateException("분석이 아직 완료되지 않았습니다.");
        }

        // 3. 중첩된 JSON 문자열을 꺼내기 (Result 안에 또 다른 JSON 문자열이 있음)
        String resultJson = jobStatus.getResult().getResultJson();

        // result.Result 내부의 악기 정보 (감지된 악기 여부, 감지 확률, 임계값)
        JobStatusResponseDto.Detection detection;

        try {
            // 기존 objectMapper 사용
            detection = objectMapper.readValue(resultJson, JobStatusResponseDto.Detection.class);
            // 결과 주입
            jobStatus.setDetection(detection);
        } catch (Exception e) {
            throw new IllegalStateException("분석 결과 파싱 실패", e);
        }

        // 4. 파싱된 결과에서 각각의 데이터 추출
        Map<String, Boolean> instruments = detection.getInstruments();      // 악기 감지 여부
        Map<String, Double> probabilities = detection.getProbabilities();  // 각 악기의 감지 확률
        Map<String, Double> thresholds = detection.getThresholds();        // 감지 임계값

        // 5. DB 저장을 위해 Map → JSON 문자열로 변환
        String instrumentsJson = toJson(instruments);
        String scoresJson = toJson(probabilities);
        String thresholdsJson = toJson(thresholds);

        // 6. jobId 기준으로 기존 InstrumentAnalysis 엔티티 조회
        InstrumentAnalysis analysis = analysisRepository.findByJobId(jobId)
            .orElseThrow(() -> new EntityNotFoundException("분석 결과를 찾을 수 없습니다. jobId = " + jobId));

        // 7. 엔티티에 분석 성공 결과 저장
        analysis.updateSuccess(
            instrumentsJson,
            scoresJson,
            thresholdsJson,
            jobStatus.getCompletedAt() != null ? jobStatus.getCompletedAt() : LocalDateTime.now()
        );

        // 8. 감지된 악기 목록만 필터링 (true 로 감지된 악기만 추출)
        List<String> detectedInstruments = instruments.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();

        // 9. 감지된 악기 목록을 DTO로 감싸서 반환
        return new AnalysisResultDto(detectedInstruments);
    }

    /**
     * 분석 Job 상태 갱신 메서드 (스케줄러 기반)
     *
     * Music.AI 분석 요청 이후 아직 완료되지 않은 Job(PENDING 상태)을 주기적으로 조회하여,
     * 분석이 완료되었을 경우 결과를 파싱하고 InstrumentAnalysis 엔티티에 저장하는 메서드입니다.
     *
     * 전체 동작 흐름:
     * 1. InstrumentAnalysis 테이블에서 status = PENDING 인 Job 목록 조회
     * 2. 각 Job에 대해 Music.AI API를 통해 현재 상태(status) 조회
     * 3. status가 SUCCEEDED가 아니면 continue (아직 분석 중)
     * 4. SUCCEEDED일 경우:
     *    - 분석 결과(resultJson)를 파싱하여 Detection 객체로 변환
     *    - 악기 목록, 신뢰도, 임계값 등을 JSON 문자열로 직렬화
     *    - InstrumentAnalysis 엔티티에 updateSuccess()로 저장
     *    - 상태를 SUCCEEDED로 변경하고 완료 시각 기록
     *
     * 예외가 발생하거나 결과가 비어 있을 경우 해당 Job은 건너뜀
     *
     * 이 메서드는 스케줄러(@Scheduled) 또는 수동 호출로 주기적 실행을 전제로 설계되었습니다.
     */
    @Transactional
    public void updatePendingAnalyses() {
        List<InstrumentAnalysis> pendingJobs =
            analysisRepository.findByStatus(AnalysisStatus.PENDING);

        for (InstrumentAnalysis analysis : pendingJobs) {
            String jobId = analysis.getJobId();

            try {
                JobStatusResponseDto jobStatus = musicAiClient.getJobResult(jobId);

                if (!"SUCCEEDED".equalsIgnoreCase(jobStatus.getStatus())) {
                    log.info("아직 완료되지 않은 Job입니다. jobId={}, status={}", jobId, jobStatus.getStatus());
                    continue;
                }

                if (jobStatus.getResult() == null || jobStatus.getResult().getResultJson() == null) {
                    log.warn("결과 필드가 비어있음. jobId={}", jobId);
                    continue;
                }

                // 결과 파싱
                JobStatusResponseDto.Detection detection = objectMapper.readValue(
                    jobStatus.getResult().getResultJson(),
                    JobStatusResponseDto.Detection.class
                );
                jobStatus.setDetection(detection);

                // JSON으로 변환
                String instrumentsJson = toJson(detection.getInstruments());
                String scoresJson = toJson(detection.getProbabilities());
                String thresholdsJson = toJson(detection.getThresholds());

                // 분석 성공 업데이트
                analysis.updateSuccess(
                    instrumentsJson,
                    scoresJson,
                    thresholdsJson,
                    jobStatus.getCompletedAt() != null ? jobStatus.getCompletedAt() : LocalDateTime.now()
                );

                log.info("분석 결과 저장 완료. jobId={}", jobId);

            } catch (Exception e) {
                log.error("분석 결과 저장 중 오류 발생. jobId={}", jobId, e);
                // 원하면 실패 상태로 저장도 가능: analysis.setStatus(FAILED)
            }
        }
    }

    // ======================
    // 헬퍼 메서드 모음
    // ======================
    /**
     * 파일 이름을 기반으로 적절한 Content-Type(MIME 타입)을 추정합니다.
     * 업로드 시 S3에 전송할 파일의 유형을 지정하기 위해 사용됩니다.
     *
     * @param fileName 업로드할 파일 이름 (예: example.mp4)
     * @return 해당 파일에 대한 MIME 타입 문자열
     */
    private String guessContentType(String fileName) {
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream"; // 알 수 없는 확장자일 경우 기본값
    }

    /**
     * 자바 객체를 JSON 문자열로 변환하는 유틸리티 메서드입니다.
     * 분석 결과를 DB에 저장하기 위해 Map 데이터를 문자열로 직렬화할 때 사용됩니다.
     *
     * @param object 변환할 객체 (Map 또는 일반 POJO 등)
     * @return 변환된 JSON 문자열
     * @throws RuntimeException 변환 중 오류 발생 시 예외 발생
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 오류", e);
        }
    }

    /**
     * Music.AI 분석 결과를 대기하면서 받아오고,
     * 최종 분석 결과를 InstrumentAnalysis 엔티티에 저장하는 통합 메서드.
     *
     * @param analysis 분석 요청 엔티티 (InstrumentAnalysis)
     * @return 분석된 악기 정보 DTO
     */
    public AnalysisResultDto waitAndParse(InstrumentAnalysis analysis) {
        String jobId = analysis.getJobId(); // ✅ 외부에서 jobId 안 넘겨도 됨

        JobStatusResponseDto jobResult = waitUntilJobSuccess(jobId, 15, 3000);

        String resultJson = Optional.ofNullable(jobResult.getResult())
            .map(JobStatusResponseDto.ResultWrapper::getResultJson)
            .orElseThrow(() -> new IllegalStateException("🎯 resultJson이 비어 있습니다."));

        try {
            JobStatusResponseDto.Detection detection =
                objectMapper.readValue(resultJson, JobStatusResponseDto.Detection.class);
            jobResult.setDetection(detection);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("🎯 resultJson 파싱 실패", e);
        }

        // 🎯 기존 updateAnalysisResult(jobId) 대신 엔티티 직접 사용
        Map<String, Boolean> instruments = jobResult.getDetection().getInstruments();
        Map<String, Double> probabilities = jobResult.getDetection().getProbabilities();
        Map<String, Double> thresholds = jobResult.getDetection().getThresholds();

        String instrumentsJson = toJson(instruments);
        String scoresJson = toJson(probabilities);
        String thresholdsJson = toJson(thresholds);

        analysis.updateSuccess(
            instrumentsJson,
            scoresJson,
            thresholdsJson,
            jobResult.getCompletedAt() != null ? jobResult.getCompletedAt() : LocalDateTime.now()
        );

        // 🎯 검출된 악기만 필터링
        List<String> detectedInstruments = instruments.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();

        return new AnalysisResultDto(detectedInstruments);
    }

    /**
     * Music.AI Job이 SUCCEEDED 상태가 될 때까지 대기하며 폴링하는 메서드
     *
     * @param jobId       분석 Job의 ID
     * @param maxAttempts 최대 시도 횟수
     * @param delayMillis 각 시도 간 대기 시간(ms)
     * @return 최종 Job 응답 (SUCCEEDED 상태일 것)
     */
    private JobStatusResponseDto waitUntilJobSuccess(String jobId, int maxAttempts, long delayMillis) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                log.info("🎯 [{} / {}] MusicAI 요청 시작 - jobId: {}", i + 1, maxAttempts, jobId); // 👈 추가
                JobStatusResponseDto response = musicAiClient.getJobResult(jobId); // 💥 여기서 터지고 있음
                String status = response.getStatus();
                log.info("🎯 [{} / {}] Job 상태: {}", i + 1, maxAttempts, status);

                if ("SUCCEEDED".equalsIgnoreCase(status)) {
                    String resultJson = response.getResult() != null ? response.getResult().getResultJson() : null;

                    if (resultJson == null || resultJson.isBlank()) {
                        log.warn("✅ 분석은 완료되었지만 resultJson이 아직 null입니다. 재시도 대기...");
                    } else {
                        return response;
                    }
                }

                if ("FAILED".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("🎯 Music.AI 분석 실패: " + status);
                }

            } catch (Exception e) {
                log.error("❌ MusicAI 요청 실패 - jobId: {}, attempt: {}", jobId, i + 1, e);
            }

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("대기 중 인터럽트 발생", e);
            }
        }

        throw new IllegalStateException("분석 시간이 초과되었습니다. (최대 시도 횟수 초과)");
    }


}