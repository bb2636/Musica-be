package com.example.musica_be.dto.wishlist;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistClassListResponseDto {
    private String status;
    private int count;
    private List<WishlistClassDto> wishlist;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistClassDto {
        private Long classId;
        private String title;
        private String thumbnailUrl;
        private String instructorName;
        private int price;
        private LocalDateTime createdAt;
    }
}

