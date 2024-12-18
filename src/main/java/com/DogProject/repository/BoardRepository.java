package com.DogProject.repository;

import com.DogProject.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 삭제되지 않은 게시글을 최신순으로 조회
    @Query("SELECT b FROM Board b WHERE b.deleteCheck = :deleteCheck ORDER BY b.bIdx DESC")
    List<Board> findByDeleteCheckOrderByBIdxDesc(@Param("deleteCheck") String deleteCheck);
    
    // 카테고리별 게시글 목록 조회
    @Query("SELECT b FROM Board b WHERE b.bType = :bType AND b.deleteCheck = :deleteCheck ORDER BY b.bIdx DESC")
    List<Board> findByBTypeAndDeleteCheck(@Param("bType") String bType, @Param("deleteCheck") String deleteCheck);
}
