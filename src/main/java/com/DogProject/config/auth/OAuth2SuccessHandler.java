package com.DogProject.config.auth;

import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
        String socialId = null;
        
        // Get registrationId from OAuth2AuthenticationToken
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        System.out.println("\n=== OAuth2 Success Handler Start ===");
        System.out.println("Registration ID: " + registrationId);
        System.out.println("Raw OAuth2 Attributes: " + attributes);
        
        try {
            // Naver 로그인
            if (attributes.containsKey("response")) {
                System.out.println("\nProcessing Naver Login:");
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                email = (String) naverResponse.get("email");
                name = (String) naverResponse.get("name");
                picture = (String) naverResponse.get("profile_image");
                provider = "naver";  // 명시적으로 "naver"로 설정
                socialId = String.valueOf(naverResponse.get("id"));
                
                System.out.println("Naver Login Details:");
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Provider: " + provider);
                System.out.println("SocialId: " + socialId);
                
                // socialId로 기존 회원 확인
                Member member = memberRepository.findByProviderAndSocialId(provider, socialId).orElse(null);
                
                if (member != null) {
                    System.out.println("\nExisting Naver Member Found:");
                    System.out.println("Member Email: " + member.getMEmail());
                    System.out.println("Member Provider: " + member.getProvider());
                    
                    member.updateOAuth2Info(name, picture, provider, socialId);
                    memberRepository.save(member);
                    setLoginSession(request, response, member);
                    response.sendRedirect("/home");
                    return;
                } else {
                    System.out.println("\nNew Naver Member - Redirecting to Join Page");
                    Member socialMember = Member.socialBuilder()
                        .name(name)
                        .mEmail(email)
                        .picture(picture)
                        .provider(provider)
                        .socialId(socialId)
                        .enabled(true)
                        .build();
                    
                    httpSession.setAttribute("socialMember", socialMember);
                    response.sendRedirect("/member/join");
                    return;
                }
            }
            
            // Google 로그인
            else if (attributes.containsKey("email") && attributes.containsKey("name")) {
                System.out.println("\nProcessing Google Login:");
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
                provider = registrationId;  // registrationId를 사용하여 provider 설정
                socialId = String.valueOf(attributes.get("sub"));
                
                System.out.println("Google Login Details:");
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Provider: " + provider);
                System.out.println("SocialId: " + socialId);
                
                Member member = memberRepository.findByProviderAndSocialId(provider, socialId).orElse(null);
                
                if (member != null) {
                    System.out.println("\nExisting Google Member Found:");
                    System.out.println("Member Email: " + member.getMEmail());
                    System.out.println("Member Provider: " + member.getProvider());
                    
                    member.updateOAuth2Info(name, picture, provider, socialId);
                    memberRepository.save(member);
                    setLoginSession(request, response, member);
                    response.sendRedirect("/home");
                    return;
                } else {
                    System.out.println("\nNew Google Member - Redirecting to Join Page");
                    Member socialMember = Member.socialBuilder()
                        .name(name)
                        .mEmail(email)
                        .picture(picture)
                        .provider(provider)
                        .socialId(socialId)
                        .enabled(true)
                        .build();
                    
                    httpSession.setAttribute("socialMember", socialMember);
                    response.sendRedirect("/member/join");
                    return;
                }
            }
            
            // Kakao 로그인
            else if (attributes.containsKey("kakao_account")) {
                System.out.println("\nProcessing Kakao Login:");
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                
                socialId = String.valueOf(attributes.get("id")); // 카카오 고유 ID 설정
                name = profile.containsKey("nickname") ? (String) profile.get("nickname") : null;
                picture = profile.containsKey("profile_image_url") ? (String) profile.get("profile_image_url") : null;
                email = kakaoAccount.containsKey("email") ? (String) kakaoAccount.get("email") : null;
                provider = registrationId;

                System.out.println("Kakao Login Details:");
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Provider: " + provider);
                System.out.println("SocialId: " + socialId);
                
                Member member = memberRepository.findByProviderAndSocialId(provider, socialId).orElse(null);
                
                if (member != null) {
                    System.out.println("\nExisting Kakao Member Found:");
                    System.out.println("Member Email: " + member.getMEmail());
                    System.out.println("Member Provider: " + member.getProvider());
                    
                    // 기존 회원 정보 업데이트 (이메일은 업데이트하지 않음)
                    member.updateOAuth2Info(name, picture, provider, socialId);
                    memberRepository.save(member);
                    
                    // 세션에 저장할 때는 기존 회원의 이메일 사용
                    email = member.getMEmail();  // 기존 회원의 이메일로 설정
                    setLoginSession(request, response, member);
                    response.sendRedirect("/home");
                    return;
                } else {
                    System.out.println("\nNew Kakao Member - Redirecting to Join Page");
                    Member socialMember = Member.socialBuilder()
                        .name(name)
                        .mEmail(email)
                        .picture(picture)
                        .provider(provider)
                        .socialId(socialId)
                        .enabled(true)
                        .build();
                    
                    httpSession.setAttribute("socialMember", socialMember);
                    response.sendRedirect("/member/join");
                    return;
                }
            }
            
            // 처리할 수 없는 소셜 로그인의 경우
            System.out.println("\nError: Unknown Provider Type");
            response.sendRedirect("/member/login?error=invalid_provider");
            
        } catch (Exception e) {
            // 오류 로깅
            System.err.println("\n=== OAuth2 Login Error ===");
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("/member/login?error=oauth2_error");
        }
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
