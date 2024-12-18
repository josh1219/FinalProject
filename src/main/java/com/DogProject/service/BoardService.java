package com.DogProject.service;

import com.DogProject.entity.Board;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final FileService fileService;

    @Transactional
    public Board createPost(String category, String title, String content, Member member, List<MultipartFile> files) {
        // 영속성 컨텍스트에서 Member 엔티티를 다시 조회
        Member managedMember = boardRepository.findById(member.getMIdx())
                .map(Board::getMember)
                .orElse(member);

        Board board = new Board();
        board.setBType(category);  // 카테고리를 bType 필드에 저장
        board.setTitle(title);
        board.setContent(content);
        board.setMember(managedMember);
        board.setInsertDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        board.setDeleteCheck("N");
        board.setReadRate(0);
        board.setLikeCount(0);
        board.setReport(0);

        // 게시글 저장
        board = boardRepository.save(board);

        // 파일 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile multipartFile : files) {
                if (!multipartFile.isEmpty()) {
                    try {
                        File file = new File();
                        file.setFType(1); // 게시글 첨부파일 타입
                        file.setTIdx(board.getBIdx()); // 게시글 번호 설정
                        fileService.saveFile(file, multipartFile);
                    } catch (Exception e) {
                        throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
                    }
                }
            }
        }

        return board;
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<Board> getBoardList() {
        return boardRepository.findAllOrderByTypeAndIdDesc("N");
    }

    // 카테고리별 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<Board> getBoardListByCategory(String category) {
        return boardRepository.findByBTypeAndDeleteCheck(category, "N");
    }

    // 게시글 번호로 게시글 조회
    public Board getBoardByIdx(int bIdx) {
        return boardRepository.findById(bIdx).orElse(null);
    }

    // 조회수 증가
    @Transactional
    public void increaseViewCount(int bIdx) {
        Board board = getBoardByIdx(bIdx);
        if (board != null) {
            board.setReadRate(board.getReadRate() + 1);
            boardRepository.save(board);
        }
    }
}
