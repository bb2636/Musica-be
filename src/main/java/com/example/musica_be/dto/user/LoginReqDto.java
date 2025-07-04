package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginReqDto {
    private String email;
    private String password;
}