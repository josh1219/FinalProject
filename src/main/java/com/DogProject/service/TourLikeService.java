package com.DogProject.service;

import com.DogProject.entity.TourLike;
import com.DogProject.entity.Member;
import com.DogProject.repository.TourLikeRepository;
import com.DogProject.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TourLikeService {
    
    @Autowired
    private TourLikeRepository tourLikeRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    public List<TourLike> getLikedPlacesByEmail(String email) {
        Optional<Member> memberOpt = memberRepository.findBymEmail(email);
        if (!memberOpt.isPresent()) {
            return Collections.emptyList();
        }
        return tourLikeRepository.findByMember(memberOpt.get());
    }
    
    public void addLikedPlace(String email, String placeName) {
        Optional<Member> memberOpt = memberRepository.findBymEmail(email);
        if (!memberOpt.isPresent()) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        
        TourLike tourLike = TourLike.builder()
                .member(memberOpt.get())
                .placeName(placeName)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        tourLikeRepository.save(tourLike);
    }
    
    public void removeLikedPlaceById(Long id, String email) {
        Optional<Member> memberOpt = memberRepository.findBymEmail(email);
        if (!memberOpt.isPresent()) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        
        Optional<TourLike> tourLikeOpt = tourLikeRepository.findById(id);
        if (!tourLikeOpt.isPresent()) {
            throw new RuntimeException("찜한 장소를 찾을 수 없습니다.");
        }
        
        TourLike tourLike = tourLikeOpt.get();
        if (!tourLike.getMember().equals(memberOpt.get())) {
            throw new RuntimeException("권한이 없습니다.");
        }
        
        tourLikeRepository.delete(tourLike);
    }
}
