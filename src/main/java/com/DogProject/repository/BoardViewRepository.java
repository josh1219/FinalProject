package com.DogProject.repository;

import com.DogProject.entity.Board;
import com.DogProject.entity.BoardView;
import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BoardViewRepository extends JpaRepository<BoardView, Long> {
    boolean existsByBoardAndMemberAndViewDate(Board board, Member member, LocalDate viewDate);
    
    // 특정 게시글의 모든 조회 기록 삭제
    void deleteByBoard(Board board);
}
