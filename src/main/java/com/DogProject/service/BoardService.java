package com.DogProject.service;

import com.DogProject.entity.Board;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.repository.BoardRepository;
import com.DogProject.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final FileService fileService;

    @Transactional
    public Board createPost(String category, String title, String content, Member member, List<MultipartFile> files) {
        Board board = new Board();
        board.setBType(category);  // 카테고리를 bType 필드에 저장
        board.setTitle(title);
        board.setContent(content);
        board.setMember(member);  // 직접 세션의 member 사용
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

    // 게시글 삭제 (DB에서 실제로 삭제)
    @Transactional
    public void deletePost(int bIdx) {
        Board board = boardRepository.findById(bIdx)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        boardRepository.delete(board);
    }

    // 게시글 수정
    @Transactional
    public Board updatePost(int bIdx, String category, String title, String content, List<MultipartFile> files, String remainingFiles) {
        // 게시글 조회
        Board board = boardRepository.findById(bIdx)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 게시글 정보 업데이트
        board.setBType(category);
        board.setTitle(title);
        board.setContent(content);

        // 게시글 저장
        board = boardRepository.save(board);

        // 삭제되지 않은 파일 ID 목록
        Set<Integer> remainingFileIds = new HashSet<>();
        if (remainingFiles != null && !remainingFiles.isEmpty()) {
            for (String fileId : remainingFiles.split(",")) {
                try {
                    remainingFileIds.add(Integer.parseInt(fileId.trim()));
                } catch (NumberFormatException e) {
                    // 잘못된 ID 형식은 무시
                }
            }
        }

        // 삭제된 파일만 삭제
        List<File> existingFiles = fileService.findAllByTypeAndIdx(1, bIdx);
        for (File file : existingFiles) {
            if (!remainingFileIds.contains(file.getFIdx())) {
                fileService.deleteFile(file);
            }
        }

        // 새로운 파일 저장
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

    /**
     * 사용자가 작성한 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Board> getMyBoards(String email) {
        return boardRepository.findByMemberEmailAndDeleteCheck(email, "N");
    }

    /**
     * 사용자가 작성한 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Board> getMyBoards(Member member) {
        return boardRepository.findByMemberAndDeleteCheck(member, "N");
    }

    // 최신 게시글 5개 조회
    @Transactional(readOnly = true)
    public List<Board> getTop5RecentPosts() {
        List<Board> allBoards = getBoardList();  // 전체 게시글 가져오기
        return allBoards.stream()
                .sorted((b1, b2) -> b2.getBIdx() - b1.getBIdx())  // bIdx 기준 내림차순 정렬
                .limit(5)  // 상위 5개만 선택
                .collect(Collectors.toList());
    }
}
