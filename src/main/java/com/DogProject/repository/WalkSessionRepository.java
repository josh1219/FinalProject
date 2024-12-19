package com.DogProject.repository;

import com.DogProject.entity.WalkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalkSessionRepository extends JpaRepository<WalkSession, Integer> {
    @Query(value = "SELECT * FROM walk_session WHERE m_idx = :mIdx AND total_distance > 0 ORDER BY walk_date ASC", nativeQuery = true)
    List<WalkSession> findByMember_mIdxOrderByWalkDateDesc(@Param("mIdx") int mIdx);

    @Query(value = "SELECT COUNT(*) FROM walk_session WHERE m_idx = :mIdx AND total_distance > 0", nativeQuery = true)
    int countByMember_mIdx(@Param("mIdx") int mIdx);
}
