package com.DogProject.repository;

import com.DogProject.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 삭제되지 않은 게시글 조회 (공지사항 우선, 나머지는 최신순)
    @Query("SELECT b FROM Board b WHERE b.deleteCheck = :deleteCheck ORDER BY CASE WHEN b.bType = '공지사항' THEN 0 ELSE 1 END, b.bIdx DESC")
    List<Board> findByDeleteCheckOrderByBIdxDesc(@Param("deleteCheck") String deleteCheck);
    
    // 카테고리별 게시글 목록 조회
    @Query("SELECT b FROM Board b WHERE b.bType = :bType AND b.deleteCheck = :deleteCheck ORDER BY b.bIdx DESC")
    List<Board> findByBTypeAndDeleteCheck(@Param("bType") String bType, @Param("deleteCheck") String deleteCheck);

    @Query("SELECT b FROM Board b " +
           "WHERE b.deleteCheck = :deleteCheck " +
           "ORDER BY " +
           "CASE b.bType " +
           "WHEN '공지사항' THEN 1 " +
           "WHEN '이벤트' THEN 2 " +
           "ELSE 3 END, " +
           "b.bIdx DESC")
    List<Board> findAllOrderByTypeAndIdDesc(@Param("deleteCheck") String deleteCheck);
}
