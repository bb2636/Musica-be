package com.example.musica_be.util;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;

public class S3PresignedUrl {

    /**
     * 업로드용 Presigned URL 생성 (PUT)
     */
    public static String generateUploadUrl(
        S3Presigner presigner,
        String bucket,
        String key,
        String contentType,
        Duration duration
    ) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(duration)
            .putObjectRequest(putRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * 다운로드용 Presigned URL 생성 (GET)
     */
    public static String generateDownloadUrl(
        S3Presigner presigner,
        String bucket,
        String key,
        Duration duration
    ) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
//            .responseContentDisposition("attachment") // 파일을 미리보기가 아닌 다운로드하도록 강제
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(duration)
            .getObjectRequest(getObjectRequest)
            .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}