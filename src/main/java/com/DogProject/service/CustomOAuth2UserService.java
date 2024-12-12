package com.DogProject.service;

import com.DogProject.dto.OAuthAttributes;
import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 디버깅을 위한 로그 추가
        System.out.println("\n=== OAuth2 Login Debug Start ===");
        System.out.println("Registration ID (Provider): " + registrationId);
        System.out.println("UserNameAttributeName: " + userNameAttributeName);
        System.out.println("OAuth2 Raw Attributes: " + oAuth2User.getAttributes());

        // 네이버 로그인의 경우 response 내부에 실제 사용자 정보가 있음
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                attributes = response;
            }
        }

        OAuthAttributes extractedAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributes);
        
        // 디버깅을 위한 로그 추가
        System.out.println("\n=== Processed OAuth2 Attributes ===");
        System.out.println("Email: " + extractedAttributes.getEmail());
        System.out.println("Name: " + extractedAttributes.getName());
        System.out.println("Provider: " + extractedAttributes.getProvider());
        System.out.println("SocialId: " + extractedAttributes.getSocialId());
        
        // provider와 socialId로 기존 회원 확인
        Member member = memberRepository.findByProviderAndSocialId(
            extractedAttributes.getProvider(), 
            extractedAttributes.getSocialId()
        ).orElse(null);
        
        System.out.println("\n=== Member Search Result ===");
        if (member != null) {
            System.out.println("Existing member found:");
            System.out.println("Member Email: " + member.getMEmail());
            System.out.println("Member Provider: " + member.getProvider());
            System.out.println("Member SocialId: " + member.getSocialId());
            
            // 기존 회원의 이메일을 extractedAttributes에 설정
            extractedAttributes = OAuthAttributes.builder()
                .name(extractedAttributes.getName())
                .email(member.getMEmail())  // 기존 회원의 이메일 사용
                .picture(extractedAttributes.getPicture())
                .provider(extractedAttributes.getProvider())
                .socialId(extractedAttributes.getSocialId())
                .attributes(extractedAttributes.getAttributes())
                .nameAttributeKey(extractedAttributes.getNameAttributeKey())
                .build();
        } else {
            System.out.println("No existing member found, creating temporary member");
        }
        
        if (member == null) {
            // 이메일로 한번 더 확인 (다른 소셜 계정으로 가입한 경우)
            member = memberRepository.findBymEmail(extractedAttributes.getEmail()).orElse(null);
            
            if (member != null && !extractedAttributes.getProvider().equals(member.getProvider())) {
                // 다른 소셜 계정으로 이미 가입된 이메일
                System.out.println("\n=== Email Conflict ===");
                System.out.println("Email already registered with different provider:");
                System.out.println("Existing Provider: " + member.getProvider());
                System.out.println("Attempted Provider: " + extractedAttributes.getProvider());
                
                httpSession.setAttribute("errorMessage", "이미 다른 소셜 계정으로 가입된 이메일입니다.");
                httpSession.setAttribute("requireRegistration", true);
                throw new OAuth2AuthenticationException("Email already registered with different provider");
            }
            
            // 새로운 회원인 경우, 세션에 임시 정보 저장
            Member tempMember = Member.socialBuilder()
                    .name(extractedAttributes.getName())
                    .mEmail(extractedAttributes.getEmail())
                    .picture(extractedAttributes.getPicture())
                    .provider(extractedAttributes.getProvider())
                    .socialId(extractedAttributes.getSocialId())
                    .enabled(true)
                    .build();
            
            System.out.println("\n=== Creating Temporary Member ===");
            System.out.println("Name: " + tempMember.getName());
            System.out.println("Email: " + tempMember.getMEmail());
            System.out.println("Provider: " + tempMember.getProvider());
            System.out.println("SocialId: " + tempMember.getSocialId());
            
            httpSession.setAttribute("socialMember", tempMember);
            httpSession.setAttribute("requireRegistration", true);
        }

        System.out.println("\n=== OAuth2 Login Debug End ===\n");

        // 네이버 로그인의 경우 response를 nameAttributeKey로 사용
        String finalAttributeKey = "naver".equals(registrationId) ? "response" : userNameAttributeName;
        
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                "naver".equals(registrationId) ? oAuth2User.getAttributes() : attributes,
                finalAttributeKey);
    }
}
