package com.example.musica_be.dto.lecture;

import lombok.Getter;

import java.util.List;

@Getter
public class LectureOrderUpdateReqDto {
    private List<LectureOrderDto> orders;

    @Getter
    public static class LectureOrderDto {
        private Long lectureId;
        private Integer order;
    }
}