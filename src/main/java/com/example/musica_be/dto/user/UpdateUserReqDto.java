package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserReqDto {
    private String name;
    private String email;
    private Long levelId;  // 수강생일 경우만 필요
}
