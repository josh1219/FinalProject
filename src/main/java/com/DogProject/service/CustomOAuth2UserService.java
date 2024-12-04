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

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        Member member = saveOrUpdate(attributes, registrationId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    @Transactional
    protected Member saveOrUpdate(OAuthAttributes attributes, String registrationId) {
        Member member = memberRepository.findBymEmail(attributes.getEmail())
                .map(entity -> {
                    entity.updateOAuth2Info(
                        attributes.getName(),
                        attributes.getPicture(),
                        attributes.getProvider(),
                        getMemberType(registrationId)
                    );
                    return entity;
                })
                .orElseGet(() -> Member.socialBuilder()
                        .name(attributes.getName())
                        .mEmail(attributes.getEmail())
                        .picture(attributes.getPicture())
                        .provider(attributes.getProvider())
                        .mType(getMemberType(registrationId))
                        .enabled(true)
                        .build());

        return memberRepository.save(member);
    }

    private String getMemberType(String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "kakao": return "2";
            case "naver": return "3";
            case "google": return "4";
            default: return "1";
        }
    }
}
