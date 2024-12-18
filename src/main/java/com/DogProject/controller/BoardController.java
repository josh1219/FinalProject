package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.DogProject.entity.Board;
import com.DogProject.entity.Member;
import com.DogProject.service.BoardService;
import com.DogProject.service.MemberService;
import com.DogProject.constant.Role;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;

    // 게시판 목록 페이지
    @GetMapping({"", "/"})
    public String boardList(Model model, @RequestParam(required = false, defaultValue = "all") String category) {
        List<Board> boardList;
        if (!category.equals("all")) {
            boardList = boardService.getBoardListByCategory(category);
        } else {
            boardList = boardService.getBoardList();
        }
        model.addAttribute("boardList", boardList);
        model.addAttribute("currentCategory", category);
        return "board";
    }

    // 글쓰기 페이지
    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal OAuth2User oauth2User, HttpServletResponse response) throws IOException {
        if (oauth2User == null) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write("<script>alert('로그인이 필요한 서비스입니다.'); window.location.href='/member/login';</script>");
            return null;
        }
        return "boardCreate";
    }

    // 게시글 작성 처리
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam("category") String category,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User oauth2User,
            HttpServletRequest request) {
        
        try {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
            }

            // 세션에서 member 가져오기
            HttpSession session = request.getSession();
            Member sessionMember = (Member) session.getAttribute("member");
            
            System.out.println("=== Session Debug ===");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session member: " + sessionMember);
            if (sessionMember != null) {
                System.out.println("Session member mIdx: " + sessionMember.getMIdx());
                System.out.println("Session member name: " + sessionMember.getName());
                System.out.println("Session member role: " + sessionMember.getRole());
            }
            
            if (sessionMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("세션이 만료되었습니다. 다시 로그인해주세요.");
            }

            // 관리자가 아닌 경우 공지사항/이벤트 카테고리 제한
            if ((category.equals("notice") || category.equals("event")) 
                && sessionMember.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("해당 카테고리는 관리자만 작성할 수 있습니다.");
            }
            
            // 게시글 저장
            Board board = boardService.createPost(category, title, content, sessionMember, files);
            
            return ResponseEntity.ok().body(board.getBIdx());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
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
