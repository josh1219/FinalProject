package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatListController {

    private final MemberService memberService;

    @GetMapping("/chatList")
    public String chatListPage(Model model, Principal principal) {
        if (principal != null) {
            Member member = memberService.findBymEmail(principal.getName());
            model.addAttribute("currentUser", member);
        }
        return "chatList";
    }
}
