package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.entity.TourLike;
import com.DogProject.repository.MemberRepository;
import com.DogProject.repository.TourLikeRepository;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour/like")
public class TourLikeController {

    private final TourLikeRepository tourLikeRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(@RequestParam String placeName, HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }

        Member member = memberRepository.findBymEmail(userEmail)
                .orElse(null);
        if (member == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "사용자를 찾을 수 없습니다."
            ));
        }

        boolean isLiked = tourLikeRepository.existsByMemberAndPlaceName(member, placeName);
        Map<String, Object> response = new HashMap<>();

        if (isLiked) {
            // 찜하기 해제
            tourLikeRepository.findByMemberAndPlaceName(member, placeName)
                    .ifPresent(tourLikeRepository::delete);
            response.put("success", true);
            response.put("liked", false);
        } else {
            // 찜하기 추가
            TourLike tourLike = TourLike.builder()
                    .member(member)
                    .placeName(placeName)
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            tourLikeRepository.save(tourLike);
            response.put("success", true);
            response.put("liked", true);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkLikeStatus(@RequestParam String placeName, HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return ResponseEntity.ok(Map.of("success", true, "liked", false));
        }

        Member member = memberRepository.findBymEmail(userEmail)
                .orElse(null);
        if (member == null) {
            return ResponseEntity.ok(Map.of("success", true, "liked", false));
        }

        boolean isLiked = tourLikeRepository.existsByMemberAndPlaceName(member, placeName);
        return ResponseEntity.ok(Map.of("success", true, "liked", isLiked));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getLikedPlaces(HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }

        Member member = memberRepository.findBymEmail(userEmail)
                .orElse(null);
        if (member == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "사용자를 찾을 수 없습니다."
            ));
        }

        List<TourLike> likedPlaces = tourLikeRepository.findByMember(member);
        return ResponseEntity.ok(Map.of("success", true, "likedPlaces", likedPlaces));
    }
}
