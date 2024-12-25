package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.entity.TourLike;
import com.DogProject.repository.MemberRepository;
import com.DogProject.repository.TourLikeRepository;
import com.DogProject.service.TourLikeService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tour/like")
public class TourLikeController {

    private final TourLikeRepository tourLikeRepository;
    private final MemberRepository memberRepository;

    @Autowired
    private TourLikeService tourLikeService;

    @GetMapping("/list")
    public String getLikedPlaces(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return "redirect:/login";
        }

        List<TourLike> likedPlaces = tourLikeService.getLikedPlacesByEmail(email);
        model.addAttribute("likedPlaces", likedPlaces);
        
        return "tour/likedPlaces";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleLike(
            @RequestParam String placeName,
            @RequestParam String category,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            HttpSession session) {
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

        Optional<TourLike> existingLike = tourLikeRepository.findByMemberAndPlaceName(member, placeName);
        boolean isLiked;
        String message;

        if (existingLike.isPresent()) {
            tourLikeRepository.delete(existingLike.get());
            isLiked = false;
            message = "찜하기가 취소되었습니다.";
        } else {
            TourLike tourLike = TourLike.builder()
                    .member(member)
                    .placeName(placeName)
                    .category(category)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            tourLikeRepository.save(tourLike);
            isLiked = true;
            message = "찜하기가 완료되었습니다.";
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "isLiked", isLiked,
            "message", message
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> unlikePlace(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            tourLikeService.removeLikedPlaceById(id, email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<?> getLikedPlacesForUser(@RequestParam(required = false) String placeName, HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "liked", false
            ));
        }

        Member member = memberRepository.findBymEmail(userEmail).orElse(null);
        if (member == null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "liked", false
            ));
        }

        if (placeName != null) {
            // 특정 장소의 찜하기 상태 확인
            boolean isLiked = tourLikeRepository.findByMemberAndPlaceName(member, placeName).isPresent();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "liked", isLiked
            ));
        } else {
            // 전체 찜한 장소 목록 반환
            List<TourLike> likedPlaces = tourLikeRepository.findByMember(member);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "likedPlaces", likedPlaces
            ));
        }
    }
}
