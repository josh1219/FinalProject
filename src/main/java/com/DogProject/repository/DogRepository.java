package com.DogProject.repository;

import com.DogProject.entity.Dog;
import com.DogProject.entity.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DogRepository extends JpaRepository<Dog, Integer> {
    Optional<Dog> findBydIdx(int d_idx);
    List<Dog> findAllByMember(Member member);
    List<Dog> findAllByMemberAndDelYN(Member member, String delYN);
}
