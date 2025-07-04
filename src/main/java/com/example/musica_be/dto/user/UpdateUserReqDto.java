package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserReqDto {
    private String name;
    private String password; // 변경할 비밀번호
}
