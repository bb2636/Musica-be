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
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(duration)
            .getObjectRequest(getRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}