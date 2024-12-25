package com.DogProject.controller;

import com.DogProject.dto.MemberDTO;
import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

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
            
            // 소셜 로그인 정보만 설정하고 name은 제외 (사용자 입력값 사용)
            memberDTO.setMEmail(socialMember.getMEmail());
            memberDTO.setPicture(socialMember.getPicture());
            memberDTO.setProvider(socialMember.getProvider());
            memberDTO.setSocialId(socialMember.getSocialId());
            memberDTO.setName(null);  // name 값을 null로 설정하여 사용자 입력값이 사용되도록 함
            
            
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

    @GetMapping("/logout")  
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
        
        return "redirect:/home";  
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/update")
    public String updateForm(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail != null) {
            Member member = memberService.findBymEmail(userEmail);
            if (member != null) {
                MemberDTO memberDTO = MemberDTO.fromEntity(member);
                String[] address = member.getAddress().split("\\|");
                String zipCode = address[0];
                String address1 = address[1];
                String address2 = address[2];

                model.addAttribute("memberZipCode", zipCode);
                model.addAttribute("memberAddress1", address1);
                model.addAttribute("memberAddress2", address2);
                model.addAttribute("memberDTO", memberDTO);
                return "member/updateMember";
            }
        }
        return "redirect:/member/login";
    }

    @PostMapping("/update")
    public String updateProcess(@Valid MemberDTO memberDTO, BindingResult bindingResult, 
                              Model model, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "member/updateMember";
        }

        try {
            String userEmail = (String) session.getAttribute("email");
            if (userEmail != null) {
                Member currentMember = memberService.findBymEmail(userEmail);
                if (currentMember != null) {
                    // 기존 정보 유지
                    memberDTO.setMEmail(currentMember.getMEmail());
                    memberDTO.setProvider(currentMember.getProvider());
                    memberDTO.setSocialId(currentMember.getSocialId());
                    memberDTO.setPicture(currentMember.getPicture());
                    memberDTO.setBirthday(currentMember.getBirthday());
                    memberDTO.setGender(currentMember.getGender());
                    
                    Member updatedMember = memberDTO.toEntity();
                    memberService.updateMember(updatedMember);
                    return "redirect:/mypage";
                }
            }
            return "redirect:/member/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/updateMember";
        }
    }
}
