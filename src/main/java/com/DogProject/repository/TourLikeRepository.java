package com.DogProject.repository;

import com.DogProject.entity.Member;
import com.DogProject.entity.TourLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourLikeRepository extends JpaRepository<TourLike, Long> {
    List<TourLike> findByMember(Member member);
    Optional<TourLike> findByMemberAndPlaceName(Member member, String placeName);
    boolean existsByMemberAndPlaceName(Member member, String placeName);
}
