package com.DogProject.service;

import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String mEmail) throws UsernameNotFoundException {
        Member member = memberRepository.findBymEmail(mEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다: " + mEmail));

        return User.builder()
                .username(member.getMEmail())
                .password(member.getMPassword())
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        if (member.getMPassword() != null) {
            member.setMPassword(passwordEncoder.encode(member.getMPassword()));
        }
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) {
        if (memberRepository.existsBymEmail(member.getMEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

    public Member findBymEmail(String mEmail) {
        return memberRepository.findBymEmail(mEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다: " + mEmail));
    }
}
