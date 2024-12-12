package com.DogProject.dto;

import com.DogProject.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String socialId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture, String provider, String socialId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
        this.socialId = socialId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        System.out.println("OAuth Provider: " + registrationId); // 디버깅용 로그 추가
        
        if("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        String socialId = attributes.get("sub").toString();  // Google의 고유 ID를 문자열로 변환
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .provider("google")
                .socialId(socialId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // 디버깅 로그 추가
        System.out.println("\n=== Naver OAuth Debug ===");
        System.out.println("Raw attributes: " + attributes);
        
        // response가 없는 경우 attributes 자체가 이미 response 내부일 수 있음
        Map<String, Object> response = attributes;
        if (attributes.containsKey("response")) {
            response = (Map<String, Object>) attributes.get("response");
            System.out.println("Found 'response' key, using inner response: " + response);
        }
        
        if (response == null) {
            throw new IllegalArgumentException("Naver attributes are invalid: " + attributes);
        }

        String socialId = String.valueOf(response.get("id"));
        String name = (String) response.get("name");
        String email = (String) response.get("email");
        String picture = (String) response.get("profile_image");

        System.out.println("Extracted values - id: " + socialId + ", name: " + name + ", email: " + email);
    
        return OAuthAttributes.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .provider("naver")
                .socialId(socialId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // 디버깅을 위한 로그 추가
        System.out.println("Kakao Raw Attributes: " + attributes);
        
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String socialId = attributes.get("id").toString();  // Kakao의 고유 ID를 문자열로 변환

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email(null)  // 이메일은 null로 설정
                .picture((String) profile.get("profile_image_url"))
                .provider("kakao")
                .socialId(socialId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Member toEntity() {
        return Member.socialBuilder()
                .name(name)
                .mEmail(email)
                .picture(picture)
                .provider(provider)
                .socialId(socialId)
                .build();
    }
}
