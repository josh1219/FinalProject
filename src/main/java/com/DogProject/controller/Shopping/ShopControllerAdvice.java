package com.DogProject.controller.Shopping;

import com.DogProject.service.Shopping.CartService;
import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ControllerAdvice(basePackages = "com.DogProject.controller.Shopping")
public class ShopControllerAdvice {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private MemberService memberService;

    @ModelAttribute
    public void addUserAttributes(Model model) {
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
                    email = ((java.util.Map<String, Object>) oauth2User.getAttribute("response")).get("email").toString();
                }
                // Kakao 로그인
                else if (oauth2User.getAttributes().containsKey("kakao_account")) {
                    String socialId = oauth2User.getAttribute("id").toString();
                    member = memberService.findByProviderAndSocialId("kakao", socialId);
                }
                
                if (member == null && email != null) {
                    member = memberService.findBymEmail(email);
                }
            }
            // 일반 로그인 사용자인 경우
            else {
                String username = auth.getName();
                member = memberService.findBymEmail(username);
            }
            
            if (member != null) {
                model.addAttribute("userName", member.getName());
                model.addAttribute("point", member.getPoint());
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }

    @ModelAttribute("cartCount")
    public int getCartCount(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return cartService.getCartItems(authentication).size();
    }
}
