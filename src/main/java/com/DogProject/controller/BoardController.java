package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    // 게시판 목록 페이지
    @GetMapping({"", "/"})
    public String boardList(Model model,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "") String searchType,
                          @RequestParam(defaultValue = "") String keyword) {
        
        // 실제 데이터베이스나 서비스에서 데이터를 가져오는 로직으로 대체 필요
        // List<BoardDto> posts = boardService.getPosts(page, searchType, keyword);

        model.addAttribute("currentPage", page);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        
        return "board";
    }

    // 글쓰기 페이지
    @GetMapping("/create")
    public String showCreateForm() {
        return "boardCreate";
    }

    // 게시글 작성 처리
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam("category") String category,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        // TODO: 게시글 저장 로직 구현
        // 1. 게시글 정보 저장
        // 2. 이미지 파일 처리 및 저장
        // 3. 저장된 게시글 정보 반환

        return ResponseEntity.ok().build();
    }

    // 게시글 상세보기 페이지
    @GetMapping("/detail")
    public String boardDetail(@RequestParam("id") Long postId, Model model) {
        // 실제 데이터베이스나 서비스에서 데이터를 가져오는 로직으로 대체 필요
        // BoardDto post = boardService.getPostById(postId);

        model.addAttribute("postId", postId);
        // model.addAttribute("post", post);

        return "boardDetail";
    }
}

// 테스트용 DTO 클래스
class BoardDto {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createdAt;
    private int viewCount;
    private int commentCount;

    public BoardDto(Long id, String title, String writer, LocalDateTime createdAt, 
                   int viewCount, int commentCount) {
        this.id = id;
        this.title = title;
        this.writer = writer;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
    }

    // Getter methods
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getWriter() { return writer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getViewCount() { return viewCount; }
    public int getCommentCount() { return commentCount; }
}
