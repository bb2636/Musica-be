package com.example.musica_be.service.instrumentAnalysis;

import com.example.musica_be.domain.instrumentAnalysis.*;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.dto.instrumentAnalysis.*;
import com.example.musica_be.repository.instrumentAnalysis.MusicAiClient;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.instrumentAnalysis.InstrumentAnalysisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstrumentAnalysisService {

    private final InstrumentAnalysisRepository analysisRepository;
    private final LectureRepository lectureRepository;
    private final MusicAiClient musicAiClient;
    private final S3Presigner s3Presigner;

    @Value("${spring.musicai.api.workflow-slug}")
    private String workflowSlug;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned URL 단독 발급
     */
    public UrlResponseDto getUrl() {
        return musicAiClient.getUrl();
    }

    /**
     * 분석 Job 단독 생성 (프론트가 GCS 업로드한 후 호출하는 경우)
     */
    public JobCreateResponseDto createJob(JobCreateRequestDto request) {
        return musicAiClient.createJob(request);
    }

    /**
     * 분석 결과 조회 (jobId 기반)
     */
    public InstrumentAnalysisResponseDto getResult(String jobId) {
        InstrumentAnalysis analysis = analysisRepository.findByJobId(jobId)
            .orElseThrow(() -> new EntityNotFoundException("Job not found with id = " + jobId));
        return InstrumentAnalysisResponseDto.from(analysis);
    }

    /**
     * 분석 전체 통합 요청
     */
    @Transactional
    public JobCreateResponseDto requestAnalysis(InstrumentAnalysisRequestDto request) {
        // 1. 강의 조회
        Lecture lecture = lectureRepository.findById(request.getLectureId())
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의 아이디입니다. => " + request.getLectureId()));

//        // 2. 업로드 URL 요청-발급 (Presigned URL 에서 uploadUrl 추출) - mp4 업로드할 위치
//        UrlResponseDto urlResponse = musicAiClient.getUrl();
//        String uploadUrl = musicAiClient.getUrl().getUploadUrl(); // 업로드 URL
//        String downloadUrl = musicAiClient.getUrl().getDownloadUrl(); // 다운로드 URL

        // 2. S3에서 전달받은 Presigned Download URL 사용
//        String s3DownloadUrl = request.getS3DownloadUrl();
        String s3DownloadUrl = "https://musica-test-bk.s3.ap-northeast-2.amazonaws.com/Czardas_in_ViolinCelloPiano.mp4?response-content-disposition=inline&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEIL%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDmFwLW5vcnRoZWFzdC0yIkcwRQIgANgkd6os9xgUAD2TnnjsmscrfrB6LiwUPpXKvOC9Y9ECIQC8ZEoaB9x2lEjgnhy%2BG9q4MKadkqwgSWC20uN0OQ3hJSrfAwiL%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAAaDDI5Mjg5OTI3NzUwMyIMIuLGCadwP1O3iZa8KrMDJLB9fzkfJhjIWrKx3zbAmw7n5g5XBV%2FlzzKmgLcbPPxL891LRKammGewufBR%2BKn%2Fs8cemN0vr7oQjl74ZioRn%2BdM65cTGHzWwXPBo771r0c44f7y9zJP%2BQe%2F4DOinbLULkiQN7EBiB1jqJHNrmxu%2Bq0XwMng4HTJRn6OXLMY7Jv91cwttUL25Z4yTqeJnMua2oXecn4wz%2BS3UJHTmG1NUW9En54bhqzAEcdqYiq0B4sE3TkVXTSLb38OCP5x81vrWjtAepc5pK3SqDCkQ9TY8jj77FZU%2FhlhYQK1gy7KZqxxo11jElsBI%2FdxqufDWxIzo38CZaUT1ESCbaObe6YV6faxEIxLL%2B12328eGRQGQx88M%2BUJKdE1QnWw8wrg5uwEPh99h2uznoZahjnAww3HlJ%2B1RPBsn69X6TF3TkwiHUYzK6z3MTSjJEnMGB9HzGeFcA53ogxiTh%2FOgrUXYJACoLGOFs0kX7aOKR6tPQCEKtzr%2BRBE4M3RhnSMkMLLrnSPFOltBsnV76oaF%2BbQazLmz%2B0BjUjwahVCfNiRb3o1ZH%2BBKfFDxm15JC5fF%2FdJ4Wc0pw%2FvMPDKs8MGOt4CXZ6NzwvScrhrA8HaVlwIMgBYNaa%2BWqjdWboGce5T4f%2FTXv%2FYHbtDX85B69QbilodXGFeqtR8hvL8r93Hl4amR3i7FLISfTJ%2Fw0MG5cv7BV35oy4UAmJpsZe1xCxezPwI590RtYLJg1eSa2auioHsLBx5AAluH%2FEDs1DQf6ITo%2BfKsN0G3gGJeluhrWHyM9KMyc8I4FwCDVFi2JtgWTB1okCd2kZuzASOwDAREAXqKomECovKVchbjvMUA2WDHZqLFXAehySn%2Bagfxg746a8STYIYgIGt8jUKKFyLu4wSGAGIQYVAakrodULKXs6CXkLkvhJ8remeouDXEfN0J%2F5QBHOdC%2ByVfE6Cy4QEiQ71FN2rqm5CwghtrKHUTc5jDYEAMeWl1rnSnHHOJStwZZ3oflgGRoQcJlw8cIi9aBscIIbs3K09PFtDUyBsEhH%2BFdinQDKetiB4PnuyXCQZ0KE%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIAUIMRIJK7TDPFJZSI%2F20250708%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Date=20250708T094724Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=81c3a1fe149017ae0b1272eba900081f02490b9d2382b26a7c7ccffc04fbd61a";

        // 3. 영상 업로드
        // 영상 업로드 로직 구현


        // 4. 분석 Job 생성
        String jobName = "job_" + UUID.randomUUID(); // 고유한 Job 이름 생성
        JobCreateRequestDto.JobParams params = new JobCreateRequestDto.JobParams(s3DownloadUrl);
        JobCreateRequestDto jobRequest = new JobCreateRequestDto(jobName, workflowSlug, params);
        JobCreateResponseDto jobResponse = musicAiClient.createJob(jobRequest);

        // 5. 분석 결과 저장
        InstrumentAnalysis analysis = InstrumentAnalysis.builder()
            .lecture(lecture)
            .jobId(jobResponse.getJobId())
            .inputFileUrl(s3DownloadUrl)
            .status(AnalysisStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();

        analysisRepository.save(analysis);

        return jobResponse;
    }
}