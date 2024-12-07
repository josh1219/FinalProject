package com.DogProject.repository;

import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findBymEmail(String mEmail);
    Optional<Member> findByProviderAndName(String provider, String name);
    boolean existsBymEmail(String mEmail);
}
