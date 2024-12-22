package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.DogProject.service.MemberService;
import com.DogProject.service.DogService;
import com.DogProject.service.WalkService;
import com.DogProject.service.BoardService;
import com.DogProject.entity.Member;
import com.DogProject.entity.Board;
import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
    
    private final MemberService memberService;
    private final DogService dogService;
    private final WalkService walkService;
    private final BoardService boardService;

    // 마이페이지 메인
    @GetMapping("")
    public String myPage(Model model, HttpSession session) {
        // 세션에서 회원 정보 가져오기
        Object mIdxObj = session.getAttribute("email");
        String email = (String) mIdxObj;
        if (email != null) {
            Member member = memberService.getMemberByEmail(email);
            model.addAttribute("member", member);
        }
        return "mypage/myPage";
    }

    // 내가 쓴 게시글 목록
    @GetMapping("/myBoards")
    public String myBoards(Model model, HttpSession session) {
        Object emailObj = session.getAttribute("email");
        if (emailObj == null) {
            return "redirect:/login";
        }
        
        String email = (String) emailObj;
        Member member = memberService.getMemberByEmail(email);
        if (member == null) {
            return "redirect:/login";
        }
        
        List<Board> myBoards = boardService.getMyBoards(member);
        model.addAttribute("member", member);
        model.addAttribute("myBoards", myBoards);
        
        return "mypage/myBoards";
    }
}
