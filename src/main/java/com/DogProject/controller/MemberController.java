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
            memberDTO.setName(socialMember.getName());
            memberDTO.setMEmail(socialMember.getMEmail());
            memberDTO.setPicture(socialMember.getPicture());
            memberDTO.setProvider(socialMember.getProvider());
            
            // 소셜 로그인 제공자별로 다르게 처리
            if ("kakao".equals(socialMember.getProvider())) {
                // 카카오는 이메일 입력 가능하도록 비워둠
                memberDTO.setMEmail("");
            }
            
            model.addAttribute("isSocialLogin", true);
            model.addAttribute("picture", socialMember.getPicture());
            model.addAttribute("provider", socialMember.getProvider());
            
            // 세션에서 소셜 로그인 정보 제거
            session.removeAttribute("socialMember");
        } else {
            memberDTO.setProvider("local"); // 일반 회원가입
            model.addAttribute("isSocialLogin", false);
        }
        
        model.addAttribute("memberDTO", memberDTO);
        return "member/join";
    }

    @PostMapping("/join")
    public String joinProcess(@Valid MemberDTO memberDTO, BindingResult bindingResult, Model model) {
        try {
            // 유효성 검사 실패 시
            if (bindingResult.hasErrors()) {
                return "member/join";
            }

            // 비밀번호 유효성 검사
            if (!memberDTO.isPasswordValid()) {
                bindingResult.rejectValue("mPassword", "error.mPassword", 
                    "비밀번호는 8자 이상이며, 영문/숫자/특수문자를 모두 포함해야 합니다.");
                return "member/join";
            }

            // DTO를 Entity로 변환
            Member member = new Member();
            member.setName(memberDTO.getName());
            member.setMEmail(memberDTO.getMEmail());
            member.setMPassword(memberDTO.getMPassword());
            member.setBirthday(memberDTO.getBirthday());
            member.setPhone(memberDTO.getPhone());
            member.setGender(memberDTO.getGender());
            member.setAddress(memberDTO.getAddress());
            member.setPicture(memberDTO.getPicture());
            // provider 값을 폼에서 전달받은 값으로 설정
            member.setProvider(memberDTO.getProvider() != null ? memberDTO.getProvider() : "local");
            member.setEnabled(true);
            member.setRole(Role.USER);  
            member.setPoint(0);
            member.setLastLoginDate(LocalDateTime.now());

            System.out.println("회원가입 - Provider 설정: " + member.getProvider());  // 로그 추가

            // 회원 저장
            memberService.saveMember(member);
            
            return "redirect:/member/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
            return "member/join";
        }
    }

    @PostMapping("/checkEmail")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean isDuplicate = memberService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
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
