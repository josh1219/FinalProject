package com.DogProject.config.auth;

import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final HttpSession httpSession;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = null;
        
        // 소셜 로그인 제공자별로 이메일 추출
        if (attributes.containsKey("email")) {
            email = (String) attributes.get("email");
        } else if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
        }
        
        if (email != null) {
            Member member = memberRepository.findBymEmail(email).orElse(null);
            
            // 첫 방문 유저이거나 회원가입이 필요한 경우
            if (member == null || httpSession.getAttribute("requireRegistration") != null) {
                httpSession.removeAttribute("requireRegistration");
                response.sendRedirect("/member/join");
                return;
            }
            
            // 이미 가입된 회원인 경우 메인 페이지로 이동
            response.sendRedirect("/");
        } else {
            // 이메일을 가져올 수 없는 경우 에러 페이지로 이동
            response.sendRedirect("/error");
        }
    }
}
