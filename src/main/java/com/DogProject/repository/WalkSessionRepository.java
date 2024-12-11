package com.DogProject.repository;

import com.DogProject.entity.Member;
import com.DogProject.entity.WalkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkSessionRepository extends JpaRepository<WalkSession, Integer> {
    List<WalkSession> findByMember_mIdxOrderByWalkDateDesc(int mIdx);

    // 특정 회원의 가장 최근 산책 기록 조회
    @Query("SELECT ws FROM WalkSession ws WHERE ws.member.mIdx = :mIdx ORDER BY ws.walkDate DESC")
    WalkSession findTopByMemberMIdxOrderByWalkDateDesc(@Param("mIdx") int mIdx);
}
