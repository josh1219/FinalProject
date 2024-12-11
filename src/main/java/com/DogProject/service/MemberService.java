package com.DogProject.service;

import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.DisabledException;
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

        // 계정 상태 업데이트
        member.updateAccountStatus();
        
        // 계정이 비활성화된 경우
        if (!member.isEnabled()) {
            throw new DisabledException("6개월 이상 로그인하지 않아 비활성화된 계정입니다.");
        }

        // 로그인 성공 시 마지막 로그인 시간 업데이트
        member.updateLastLoginDate();
        memberRepository.save(member);

        return new User(member.getMEmail(),
                member.getMPassword(),
                member.isEnabled(),
                true, // account non expired
                true, // credentials non expired
                true, // account non locked
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
    }

    @Transactional
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        validateRequiredFields(member);
        
        // 모든 회원가입에 대해 비밀번호 암호화
        if (member.getMPassword() != null) {
            member.setMPassword(passwordEncoder.encode(member.getMPassword()));
        }
        
        return memberRepository.save(member);
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
    }

    private void validateDuplicateMember(Member member) {
        if (member.getProvider() != null && !member.getProvider().equals("local")) {
            memberRepository.findByProviderAndSocialId(member.getProvider(), member.getSocialId())
                    .ifPresent(m -> {
                        throw new IllegalStateException("이미 가입된 회원입니다.");
                    });
        } else {
            memberRepository.findByEmailIgnoreCase(member.getMEmail())
                    .ifPresent(m -> {
                        throw new IllegalStateException("이미 가입된 회원입니다.");
                    });
        }
    }

    public boolean existsByEmail(String email) {
        // local 회원과 소셜 로그인 회원의 이메일을 모두 확인
        return memberRepository.existsBymEmail(email);
    }

    public Member findBymEmail(String mEmail) {
        return memberRepository.findBymEmail(mEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다: " + mEmail));
    }

    public Member findByMIdx(int mIdx) {
        return memberRepository.findById(mIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. ID: " + mIdx));
    }
}
