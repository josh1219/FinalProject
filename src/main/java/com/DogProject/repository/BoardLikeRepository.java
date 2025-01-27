package com.DogProject.repository;

import com.DogProject.entity.Board;
import com.DogProject.entity.BoardLike;
import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    boolean existsByBoardAndMember(Board board, Member member);
    void deleteByBoardAndMember(Board board, Member member);
    
    // 특정 게시글의 모든 좋아요 삭제
    void deleteByBoard(Board board);
}
