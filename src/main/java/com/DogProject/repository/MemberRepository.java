package com.DogProject.repository;

import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findBymEmail(String mEmail);
    
    // 소셜 로그인 회원 조회
    Optional<Member> findByProviderAndSocialId(String provider, String socialId);
    boolean existsBymEmail(String mEmail);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.mEmail = :email AND m.provider != 'local'")
    boolean existsBySocialEmail(@Param("email") String email);

    @Query("SELECT m FROM Member m WHERE LOWER(m.mEmail) = LOWER(:email)")
    Optional<Member> findByEmailIgnoreCase(@Param("email") String email);
}
