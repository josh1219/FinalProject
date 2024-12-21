package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.entity.Board;
import com.DogProject.service.MemberService;
import com.DogProject.service.BoardService;
import com.DogProject.constant.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;
    private final BoardService boardService;
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
                    String socialId = oauth2User.getAttribute("id").toString();
                    member = memberService.findByProviderAndSocialId("kakao", socialId);
                }
                
                if (member == null && email != null) {
                    member = memberService.findByEmail(email);
                }
            }
            
            if (member != null) {
                model.addAttribute("member", member);
                if (member.getRole() == Role.ADMIN) {
                    model.addAttribute("isAdmin", true);
                }
            }
        }

        // 최근 게시글 5개 가져오기
        List<Board> recentBoards = boardService.getBoardList().stream()
                .filter(board -> "N".equals(board.getDeleteCheck()))
                .sorted((b1, b2) -> b2.getBIdx() - b1.getBIdx())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentBoards", recentBoards);
        
        return "home/home";
    }

    @GetMapping("/home01")
    public String home01() {
        return "home/home01";
    }

    @GetMapping("/tourmap")
    public String tourmap() {
        return "tourmap";
    }

    @GetMapping("/")
    public String root(Model model) {
        // 루트 경로 접근 시 home 페이지로 리다이렉트
        return "redirect:/home";
    }
}
