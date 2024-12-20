package com.DogProject.repository;

import com.DogProject.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByBoard_bIdxAndDeleteCheckOrderByInsertDateDesc(int boardId, String deleteCheck);
}
