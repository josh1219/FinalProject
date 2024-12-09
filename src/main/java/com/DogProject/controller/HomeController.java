package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;
    private final HttpSession httpSession;

    @GetMapping("/home")
    public String home(Model model) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            Member member = null;
            
            // OAuth2 로그인 사용자인 경우
            if (auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
                String email = null;
                
                // Google 로그인
                if (oauth2User.getAttributes().containsKey("email")) {
                    email = oauth2User.getAttribute("email");
                }
                // Naver 로그인
                else if (oauth2User.getAttributes().containsKey("response")) {
                    email = ((Map<String, Object>) oauth2User.getAttribute("response")).get("email").toString();
                }
                // Kakao 로그인
                else if (oauth2User.getAttributes().containsKey("kakao_account")) {
                    Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
                    if (kakaoAccount.containsKey("email")) {
                        email = kakaoAccount.get("email").toString();
                    }
                }
                
                if (email != null) {
                    member = memberService.findBymEmail(email);
                }
            }
            // 일반 로그인 사용자인 경우
            else {
                String username = auth.getName();
                member = memberService.findBymEmail(username);
            }
            
            if (member != null) {
                model.addAttribute("member", member);
                model.addAttribute("isLoggedIn", true);
                System.out.println("Home - 로그인 사용자 정보:");
                System.out.println("Email: " + member.getMEmail());
                System.out.println("Name: " + member.getName());
                System.out.println("Provider: " + member.getProvider());
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        
        return "home";
    }
}
