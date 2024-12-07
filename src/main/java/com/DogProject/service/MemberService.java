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

        return new User(member.getMEmail(),
                member.getMPassword(),
                true, // enabled
                true, // account non expired
                true, // credentials non expired
                true, // account non locked
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
    }

    @Transactional
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        
        
        // 비밀번호가 있는 경우 암호화 (모든 회원 타입)
        if (member.getMPassword() != null) {
            member.setMPassword(passwordEncoder.encode(member.getMPassword()));
        }
        
        // 필수 필드 검증
        validateRequiredFields(member);
        
        Member savedMember = memberRepository.save(member);
        return savedMember;
    }

    private void validateRequiredFields(Member member) {
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력값입니다.");
        }
        if (member.getMEmail() == null || member.getMEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 입력값입니다.");
        }
        if (member.getBirthday() == null || member.getBirthday().trim().isEmpty()) {
            throw new IllegalArgumentException("생년월일은 필수 입력값입니다.");
        }
        if (member.getPhone() == null || member.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수 입력값입니다.");
        }
        if (member.getGender() == null || member.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("성별은 필수 입력값입니다.");
        }
        if (member.getAddress() == null || member.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수 입력값입니다.");
        }
        // 모든 회원 타입에 대해 비밀번호 필수
        if (member.getMPassword() == null || member.getMPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }
    }

    private void validateDuplicateMember(Member member) {
        if (memberRepository.existsBymEmail(member.getMEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsBymEmail(email);
    }

    public Member findBymEmail(String mEmail) {
        return memberRepository.findBymEmail(mEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다: " + mEmail));
    }
}
