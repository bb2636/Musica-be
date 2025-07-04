package com.example.musica_be.dto.wishlist;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistActionResponseDto {
    private String status;
    private String message;
    private TargetDto target;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TargetDto {
        private String type; // "class"
        private Long id;
    }
}

