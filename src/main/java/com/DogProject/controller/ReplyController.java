package com.DogProject.controller;

import com.DogProject.entity.Board;
import com.DogProject.entity.Member;
import com.DogProject.entity.Reply;
import com.DogProject.service.BoardService;
import com.DogProject.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final BoardService boardService;

    @PostMapping("/add")
    public ResponseEntity<String> addReply(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Member member = (Member) session.getAttribute("sessionMember");
            if (member == null) {
                System.out.println("Session member is null");
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            String content = (String) request.get("content");
            Object boardIdObj = request.get("boardId");
            
            System.out.println("Raw request data: " + request);
            System.out.println("Content: " + content);
            System.out.println("Board ID object: " + boardIdObj);
            System.out.println("Board ID class: " + (boardIdObj != null ? boardIdObj.getClass().getName() : "null"));

            // boardId를 안전하게 변환
            Integer boardId;
            if (boardIdObj instanceof Integer) {
                boardId = (Integer) boardIdObj;
            } else if (boardIdObj instanceof String) {
                try {
                    boardId = Integer.parseInt((String) boardIdObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("잘못된 게시글 ID 형식입니다.");
                }
            } else if (boardIdObj instanceof Double) {
                boardId = ((Double) boardIdObj).intValue();
            } else if (boardIdObj instanceof Long) {
                boardId = ((Long) boardIdObj).intValue();
            } else {
                return ResponseEntity.badRequest().body("게시글 ID가 유효하지 않습니다.");
            }

            if (content == null || content.trim().isEmpty()) {
                System.out.println("Content is empty");
                return ResponseEntity.badRequest().body("댓글 내용을 입력해주세요.");
            }

            Board board = boardService.getBoardByIdx(boardId);
            if (board == null) {
                System.out.println("Board not found with ID: " + boardId);
                return ResponseEntity.badRequest().body("게시글을 찾을 수 없습니다.");
            }

            Reply reply = Reply.builder()
                    .content(content)
                    .board(board)
                    .member(member)
                    .insertDate(LocalDateTime.now())
                    .deleteCheck("N")
                    .build();

            replyService.save(reply);
            return ResponseEntity.ok("댓글이 등록되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();  // 스택 트레이스 출력
            System.out.println("Error adding reply: " + e.getMessage());
            return ResponseEntity.badRequest().body("댓글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/addReply")
    public ResponseEntity<String> addReplyToComment(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Member member = (Member) session.getAttribute("sessionMember");
            if (member == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            String content = (String) request.get("content");
            Object parentIdObj = request.get("parentId");
            
            System.out.println("Raw request data: " + request);
            System.out.println("Content: " + content);
            System.out.println("Parent ID object: " + parentIdObj);

            // parentId를 안전하게 변환
            Integer parentId;
            if (parentIdObj instanceof Integer) {
                parentId = (Integer) parentIdObj;
            } else if (parentIdObj instanceof String) {
                try {
                    parentId = Integer.parseInt((String) parentIdObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("잘못된 부모 댓글 ID 형식입니다.");
                }
            } else if (parentIdObj instanceof Double) {
                parentId = ((Double) parentIdObj).intValue();
            } else if (parentIdObj instanceof Long) {
                parentId = ((Long) parentIdObj).intValue();
            } else {
                return ResponseEntity.badRequest().body("부모 댓글 ID가 유효하지 않습니다.");
            }

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("답글 내용을 입력해주세요.");
            }

            Reply parentReply = replyService.findById(parentId);
            if (parentReply == null) {
                return ResponseEntity.badRequest().body("원본 댓글을 찾을 수 없습니다.");
            }

            Reply reply = Reply.builder()
                    .content(content)
                    .board(parentReply.getBoard())
                    .member(member)
                    .parent(parentReply)
                    .depth(parentReply.getDepth() + 1)
                    .insertDate(LocalDateTime.now())
                    .deleteCheck("N")
                    .build();

            replyService.save(reply);
            return ResponseEntity.ok("답글이 등록되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("답글 등록 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateReply(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Member member = (Member) session.getAttribute("sessionMember");
            if (member == null) {
                System.out.println("Session member is null");
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            Integer replyId = (Integer) request.get("rIdx");
            String content = (String) request.get("content");

            System.out.println("Reply ID: " + replyId);
            System.out.println("Content: " + content);

            if (content == null || content.trim().isEmpty()) {
                System.out.println("Content is empty");
                return ResponseEntity.badRequest().body("댓글 내용을 입력해주세요.");
            }

            Reply reply = replyService.findById(replyId);
            if (reply == null) {
                System.out.println("Reply not found with ID: " + replyId);
                return ResponseEntity.badRequest().body("댓글을 찾을 수 없습니다.");
            }

            if (reply.getMember().getMIdx() != member.getMIdx() && 
                !member.getRole().name().equals("ADMIN")) {
                System.out.println("Member does not have permission to update reply");
                return ResponseEntity.badRequest().body("댓글 수정 권한이 없습니다.");
            }

            reply.setContent(content);
            replyService.save(reply);
            return ResponseEntity.ok("댓글이 수정되었습니다.");
        } catch (Exception e) {
            System.out.println("Error updating reply: " + e.getMessage());
            return ResponseEntity.badRequest().body("댓글 수정 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteReply(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Member member = (Member) session.getAttribute("sessionMember");
            if (member == null) {
                System.out.println("Session member is null");
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            Integer replyId = (Integer) request.get("rIdx");
            Reply reply = replyService.findById(replyId);
            if (reply == null) {
                System.out.println("Reply not found with ID: " + replyId);
                return ResponseEntity.badRequest().body("댓글을 찾을 수 없습니다.");
            }

            if (reply.getMember().getMIdx() != member.getMIdx() && 
                !member.getRole().name().equals("ADMIN")) {
                System.out.println("Member does not have permission to delete reply");
                return ResponseEntity.badRequest().body("댓글 삭제 권한이 없습니다.");
            }

            reply.setDeleteCheck("Y");
            replyService.save(reply);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            System.out.println("Error deleting reply: " + e.getMessage());
            return ResponseEntity.badRequest().body("댓글 삭제 중 오류가 발생했습니다.");
        }
    }
}
