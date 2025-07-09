package com.example.musica_be.service.wishlist;

import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.wishlist.WishlistActionResponseDto;
import com.example.musica_be.dto.wishlist.WishlistClassListResponseDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.repository.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository; // 또는 로그인 유저로 대체
    private final ClassesRepository classesRepository; // classIs 유효성 체크용

    public WishlistActionResponseDto addWishlist(long userId, long classId) {
        // 클래스 존재 여부 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("해당 클래스가 존재하지 않습니다."));

        // 유저 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 찜 증복 체크
        if (wishlistRepository.existsByUserAndClasses(user, classes)) {
            throw new IllegalArgumentException("이미 찜한 클래스입니다.");
        }

        // 위시리스트에 저장
        Wishlist wishlist = Wishlist.builder()
            .user(user)
            .classes(classes)
            .createdAt(LocalDateTime.now())
            .build();
        wishlistRepository.save(wishlist);

        // 응답 반환
        return WishlistActionResponseDto.builder()
            .status("success")
            .message("찜 등록이 완료되었습니다.")
            .target(WishlistActionResponseDto.TargetDto.builder()
                .type("class")
                .id(classId)
                .build())
            .build();

    }

    public WishlistActionResponseDto deleteWishlist(long userId, long classId) {
        // 클래스 존재 여부 확인
        Classes classes = classesRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("해당 클래스가 존재하지 않습니다."));

        // 유저 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 찜 존재 여부 확인 및 삭제
        Wishlist wishlist = wishlistRepository.findByUserAndClasses(user, classes)
            .orElseThrow(() -> new IllegalArgumentException("해당 찜이 존재하지 않습니다."));

        wishlistRepository.delete(wishlist);

        // 응답 반환
        return WishlistActionResponseDto.builder()
            .status("success")
            .message("찜 해제가 완료되었습니다.")
            .target(WishlistActionResponseDto.TargetDto.builder()
                .type("class")
                .id(classId)
                .build())
            .build();
    }

    public WishlistClassListResponseDto getWishlistClasses(long userId) {
        // 유저 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        // 찜 목록 가져오기
        List<Wishlist> wishlistList = wishlistRepository.findAllByUser(user);

        // DTO 로 변환
        List<WishlistClassListResponseDto.WishlistClassDto> result = wishlistList.stream()
            .map(wishlist -> {
                Classes c = wishlist.getClasses();
                return WishlistClassListResponseDto.WishlistClassDto.builder()
                    .classId(c.getId())
                    .title(c.getTitle())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .instructorName(c.getInstructor().getName())
                    .price(c.getClassPrice())
                    .createdAt(wishlist.getCreatedAt())
                    .build();
            }).toList();

        return WishlistClassListResponseDto.builder()
            .status("success")
            .count(result.size())
            .wishlist(result)
            .build();
    }

}
