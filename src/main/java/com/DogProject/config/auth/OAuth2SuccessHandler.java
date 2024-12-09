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
import javax.servlet.http.Cookie;
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
        String name = null;
        String picture = null;
        String provider = null;
        
        // Google 로그인
        if (attributes.containsKey("email") && attributes.containsKey("name")) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
            provider = "google";
            
            // 구글 로그인의 경우 이메일로 기존 회원 확인
            Member member = memberRepository.findBymEmail(email).orElse(null);
            
            if (member != null) {
                // 이미 가입된 회원인 경우 세션 설정 후 홈으로 이동
                setLoginSession(request, response, member);
                response.sendRedirect("/home");
                return;
            } else {
                // 새로운 회원인 경우 회원가입 페이지로 이동
                Member socialMember = new Member();
                socialMember.setMEmail(email);
                socialMember.setName(name);
                socialMember.setPicture(picture);
                socialMember.setProvider(provider);
                
                httpSession.setAttribute("socialMember", socialMember);
                response.sendRedirect("/member/join");
                return;
            }
        }
        // Kakao 로그인
        else if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
            picture = (String) profile.get("profile_image_url");
            email = kakaoAccount.containsKey("email") ? (String) kakaoAccount.get("email") : null;
            provider = "kakao";
            
            // 카카오 로그인의 경우 이메일로 기존 회원 확인
            Member member = memberRepository.findBymEmail(email).orElse(null);
            
            if (member != null) {
                // 이미 가입된 회원인 경우 세션 설정 후 홈으로 이동
                setLoginSession(request, response, member);
                response.sendRedirect("/home");
                return;
            } else {
                // 새로운 회원인 경우 회원가입 페이지로 이동
                Member socialMember = new Member();
                socialMember.setMEmail(email);
                socialMember.setName(name);
                socialMember.setPicture(picture);
                socialMember.setProvider(provider);
                
                httpSession.setAttribute("socialMember", socialMember);
                response.sendRedirect("/member/join");
                return;
            }
        }
        // Naver 로그인
        else if (attributes.containsKey("response")) {
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            email = (String) naverResponse.get("email");
            name = (String) naverResponse.get("name");
            picture = (String) naverResponse.get("profile_image");
            provider = "naver";
            
            // 네이버 로그인의 경우 이메일로 기존 회원 확인
            Member member = memberRepository.findBymEmail(email).orElse(null);
            
            if (member != null) {
                // 이미 가입된 회원인 경우 세션 설정 후 홈으로 이동
                setLoginSession(request, response, member);
                response.sendRedirect("/home");
                return;
            } else {
                // 새로운 회원인 경우 회원가입 페이지로 이동
                Member socialMember = new Member();
                socialMember.setMEmail(email);
                socialMember.setName(name);
                socialMember.setPicture(picture);
                socialMember.setProvider(provider);
                
                httpSession.setAttribute("socialMember", socialMember);
                response.sendRedirect("/member/join");
                return;
            }
        }
        
        // 처리되지 않은 경우 로그인 페이지로 리다이렉트
        response.sendRedirect("/member/login");
    }
    
    // 세션 설정 메소드
    private void setLoginSession(HttpServletRequest request, HttpServletResponse response, Member member) {
        // 세션에 로그인 정보 저장
        httpSession.setAttribute("member", member);
        httpSession.setAttribute("isLoggedIn", true);
        httpSession.setAttribute("mIdx", member.getMIdx()); 
        
        // 쿠키에 사용자 정보 저장
        String userInfo = member.getMIdx() + "★" + member.getMEmail() + "★" + member.getProvider() + "★" + member.getRole(); 
        Cookie cookie = new Cookie("USER_INFO", userInfo);
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1시간
        response.addCookie(cookie);
    }
}
