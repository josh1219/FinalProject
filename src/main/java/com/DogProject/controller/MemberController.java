package com.DogProject.controller;

import com.DogProject.dto.MemberDTO;
import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import com.DogProject.constant.Role;  
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/join")
    public String joinForm(Model model, HttpSession session) {
        // 세션에서 소셜 로그인 정보 확인
        Member socialMember = (Member) session.getAttribute("socialMember");
        MemberDTO memberDTO = new MemberDTO();
        
        if (socialMember != null) {
            // 소셜 로그인 정보가 있으면 DTO에 설정
            System.out.println("Social Member found in session:");
            System.out.println("Name: " + socialMember.getName());
            System.out.println("Email: " + socialMember.getMEmail());
            System.out.println("Provider: " + socialMember.getProvider());
            System.out.println("SocialId: " + socialMember.getSocialId());
            
            memberDTO.setName(socialMember.getName());
            memberDTO.setMEmail(socialMember.getMEmail());
            memberDTO.setPicture(socialMember.getPicture());
            memberDTO.setProvider(socialMember.getProvider());
            memberDTO.setSocialId(socialMember.getSocialId());
            
            System.out.println("MemberDTO after conversion:");
            System.out.println("Name: " + memberDTO.getName());
            System.out.println("Email: " + memberDTO.getMEmail());
            System.out.println("Provider: " + memberDTO.getProvider());
            System.out.println("SocialId: " + memberDTO.getSocialId());
            
            // 세션에서 소셜 로그인 정보 제거
            session.removeAttribute("socialMember");
        } else {
            System.out.println("No social member found in session, using local provider");
            memberDTO.setProvider("local"); // 일반 회원가입
        }
        
        model.addAttribute("memberDTO", memberDTO);
        return "member/join";
    }

    @PostMapping("/join")
    public String joinProcess(@Valid MemberDTO memberDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/join";
        }

        try {
            // 일반 회원가입일 경우 비밀번호 유효성 검사
            if ("local".equals(memberDTO.getProvider()) && !memberDTO.isPasswordValid()) {
                bindingResult.rejectValue("mPassword", "error.mPassword", 
                    "비밀번호는 8자 이상이며, 영문/숫자/특수문자를 모두 포함해야 합니다.");
                return "member/join";
            }

            Member member = memberDTO.toEntity();
            memberService.saveMember(member);
            return "redirect:/member/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
    }

    @PostMapping("/checkEmail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        // 이메일 중복 체크
        boolean exists = memberService.existsByEmail(email);
        
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "member/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // 모든 쿠키 삭제
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
            
            // SecurityContext 클리어
            SecurityContextHolder.clearContext();
        }
        
        return "redirect:/member/login";
    }
}
