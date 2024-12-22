package com.DogProject.repository;

import com.DogProject.entity.Path;
import com.DogProject.entity.WalkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PathRepository extends JpaRepository<Path, Integer> {
    @Query("SELECT p FROM Path p JOIN FETCH p.walkSession JOIN FETCH p.member WHERE p.walkSession.wsIdx = :wsIdx ORDER BY p.createTime ASC")
    List<Path> findPathsByWalkSessionOrderBySequence(@Param("wsIdx") int wsIdx);

    @Query("SELECT COUNT(p) FROM Path p WHERE p.walkSession = :walkSession")
    Long countByWalkSession(@Param("walkSession") WalkSession walkSession);

    // 가장 최근 시퀀스 조회
    Optional<Path> findTopByWalkSessionOrderBySequenceDesc(WalkSession walkSession);
}
