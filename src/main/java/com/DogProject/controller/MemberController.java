package com.DogProject.controller;

import com.DogProject.dto.MemberDTO;
import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
            memberDTO.setMType(socialMember.getMType());
            memberDTO.setProvider(socialMember.getProvider());
            model.addAttribute("isSocialLogin", true);
            
            // 세션에서 소셜 로그인 정보 제거
            session.removeAttribute("socialMember");
        } else {
            memberDTO.setMType("1"); // 일반 회원가입
            model.addAttribute("isSocialLogin", false);
        }
        
        model.addAttribute("memberDTO", memberDTO);
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@Valid MemberDTO memberDTO, BindingResult bindingResult, Model model) {
        // 일반 회원가입인 경우 비밀번호 유효성 검사
        if ("1".equals(memberDTO.getMType()) && !memberDTO.isPasswordValid()) {
            bindingResult.addError(new ObjectError("memberDTO", 
                "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다."));
        }

        if (bindingResult.hasErrors()) {
            // 소셜 로그인 여부 다시 설정
            model.addAttribute("isSocialLogin", !"1".equals(memberDTO.getMType()));
            return "member/join";
        }

        try {
            Member member = Member.userBuilder()
                    .name(memberDTO.getName())
                    .mEmail(memberDTO.getMEmail())
                    .mPassword(memberDTO.getMPassword())
                    .birthday(memberDTO.getBirthday())
                    .phone(memberDTO.getPhone())
                    .gender(memberDTO.getGender())
                    .address(memberDTO.getAddress())
                    .picture(memberDTO.getPicture())
                    .provider(memberDTO.getProvider())
                    .mType(memberDTO.getMType())
                    .build();

            memberService.saveMember(member);
            return "redirect:/member/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // 소셜 로그인 여부 다시 설정
            model.addAttribute("isSocialLogin", !"1".equals(memberDTO.getMType()));
            return "member/join";
        }
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
}
