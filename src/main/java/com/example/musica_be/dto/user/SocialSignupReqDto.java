package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialSignupReqDto {
    private String email;
    private String name;
    private String role;
    private Long levelId;
}
