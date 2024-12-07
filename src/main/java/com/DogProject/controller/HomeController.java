package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            Member member = memberService.findBymEmail(user.getUsername());
            System.out.println("Home - 로그인 사용자 정보:");
            System.out.println("Email: " + member.getMEmail());
            System.out.println("Provider: " + member.getProvider());
            model.addAttribute("member", member);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        return "home";
    }
}
