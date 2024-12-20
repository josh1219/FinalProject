package com.DogProject.service;

import com.DogProject.entity.Reply;
import com.DogProject.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    @Transactional
    public Reply save(Reply reply) {
        return replyRepository.save(reply);
    }

    @Transactional(readOnly = true)
    public Reply findById(int id) {
        return replyRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Reply> findByBoardId(int boardId) {
        return replyRepository.findByBoard_bIdxAndDeleteCheckOrderByInsertDateDesc(boardId, "N");
    }
}
