package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.DogProject.entity.Board;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.service.BoardService;
import com.DogProject.service.FileService;
import com.DogProject.service.MemberService;
import com.DogProject.constant.Role;
import com.DogProject.repository.FileRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;
    private final FileService fileService;
    private final FileRepository fileRepository;

    // 게시판 목록 페이지
    @GetMapping({"", "/"})
    public String boardList(Model model, @RequestParam(required = false, defaultValue = "전체") String category) {
        List<Board> boardList;
        if (!category.equals("전체")) {
            boardList = boardService.getBoardListByCategory(category);
        } else {
            boardList = boardService.getBoardList();
        }
        
        // 각 게시글의 첨부파일 개수 확인
        Map<Integer, Integer> attachmentCounts = new HashMap<>();
        for (Board board : boardList) {
            List<File> files = fileRepository.findAllByTypeAndIdx(1, board.getBIdx());
            attachmentCounts.put(board.getBIdx(), files.size());
        }
        
        model.addAttribute("boardList", boardList);
        model.addAttribute("attachmentCounts", attachmentCounts);
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

    // 게시글 상세 페이지
    @GetMapping("/detail")
    public String boardDetail(@RequestParam("id") int bIdx, Model model, HttpSession session) {
        try {
            // 게시글 조회
            Board board = boardService.getBoardByIdx(bIdx);
            if (board == null || board.getDeleteCheck().equals("Y")) {
                return "redirect:/board?error=notfound";
            }

            // 현재 로그인한 사용자 정보를 세션에서 가져오기
            Member sessionMember = (Member) session.getAttribute("member");
            
            // 디버그 로그 추가
            System.out.println("=== Session Debug in BoardDetail ===");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Member: " + sessionMember);
            if (sessionMember != null) {
                System.out.println("Session Member mIdx: " + sessionMember.getMIdx());
                System.out.println("Board Member mIdx: " + board.getMember().getMIdx());
                System.out.println("Is Same User: " + (sessionMember.getMIdx() == board.getMember().getMIdx()));
            } else {
                System.out.println("No session member found");
            }

            // 조회수 증가 (자신의 게시글이 아닐 경우에만)
            if (sessionMember == null || sessionMember.getMIdx() != board.getMember().getMIdx()) {
                boardService.increaseViewCount(bIdx);
                board = boardService.getBoardByIdx(bIdx); // 업데이트된 정보 다시 조회
            }

            // 게시글의 첨부 파일 조회 (fType이 1인 경우 게시글 첨부파일)
            List<File> boardFiles = fileService.findAllByTypeAndIdx(1, bIdx);
            if (!boardFiles.isEmpty()) {
                model.addAttribute("boardFiles", boardFiles);
            }

            model.addAttribute("board", board);
            model.addAttribute("sessionMember", sessionMember); // 세션 멤버 정보 전달
            
            // 작성자 본인인지 여부를 추가
            boolean isAuthor = sessionMember != null && sessionMember.getMIdx() == board.getMember().getMIdx();
            model.addAttribute("isAuthor", isAuthor);
            
            return "boardDetail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/board?error=error";
        }
    }

    // 게시글 삭제
    @GetMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePost(@PathVariable("id") int bIdx, HttpSession session) {
        try {
            // 게시글 조회
            Board board = boardService.getBoardByIdx(bIdx);
            if (board == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("게시글을 찾을 수 없습니다.");
            }

            // 현재 로그인한 사용자 정보를 세션에서 가져오기
            Member sessionMember = (Member) session.getAttribute("member");
            
            // 권한 체크: 작성자 본인이거나 관리자만 삭제 가능
            if (sessionMember == null || 
                (sessionMember.getMIdx() != board.getMember().getMIdx() && 
                 sessionMember.getRole() != Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("삭제 권한이 없습니다.");
            }

            // 게시글 삭제 (DB에서 실제로 삭제)
            boardService.deletePost(bIdx);
            
            return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("삭제 중 오류가 발생했습니다.");
        }
    }

    // 게시글 수정 페이지
    @GetMapping("/edit")
    public String showUpdateForm(@RequestParam("id") int bIdx, Model model, HttpSession session) {
        try {
            // 게시글 조회
            Board board = boardService.getBoardByIdx(bIdx);
            if (board == null) {
                return "redirect:/board?error=notfound";
            }

            // 현재 로그인한 사용자 정보를 세션에서 가져오기
            Member sessionMember = (Member) session.getAttribute("member");
            
            // 권한 체크: 작성자 본인만 수정 가능
            if (sessionMember == null || sessionMember.getMIdx() != board.getMember().getMIdx()) {
                return "redirect:/board?error=unauthorized";
            }

            // 게시글의 첨부 파일 조회
            List<File> boardFiles = fileService.findAllByTypeAndIdx(1, bIdx);
            if (!boardFiles.isEmpty()) {
                model.addAttribute("boardFiles", boardFiles);
            }

            model.addAttribute("board", board);
            model.addAttribute("sessionMember", sessionMember);
            return "boardUpdate";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/board?error=error";
        }
    }

    // 게시글 수정 처리
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updatePost(
            @RequestParam("bIdx") int bIdx,
            @RequestParam("category") String category,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "remainingFiles", required = false) String remainingFiles,
            HttpSession session) {
        
        try {
            // 게시글 조회
            Board board = boardService.getBoardByIdx(bIdx);
            if (board == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("게시글을 찾을 수 없습니다.");
            }

            // 현재 로그인한 사용자 정보를 세션에서 가져오기
            Member sessionMember = (Member) session.getAttribute("member");
            
            // 권한 체크: 작성자 본인만 수정 가능
            if (sessionMember == null || sessionMember.getMIdx() != board.getMember().getMIdx()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("수정 권한이 없습니다.");
            }

            // 관리자가 아닌 경우 공지사항/이벤트 카테고리 제한
            if ((category.equals("notice") || category.equals("event")) 
                && sessionMember.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("해당 카테고리는 관리자만 작성할 수 있습니다.");
            }

            // 게시글 수정
            board = boardService.updatePost(bIdx, category, title, content, files, remainingFiles);
            
            return ResponseEntity.ok(board.getBIdx());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("수정 중 오류가 발생했습니다: " + e.getMessage());
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

}
