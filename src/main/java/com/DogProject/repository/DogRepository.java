package com.DogProject.repository;

import com.DogProject.entity.Dog;
import com.DogProject.entity.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DogRepository extends JpaRepository<Dog, Integer> {
    Optional<Dog> findBydIdx(int d_idx);
    List<Dog> findAllByMember(Member member);
    List<Dog> findAllByMemberAndDelYN(Member member, String delYN);
    @Query("SELECT d FROM Dog d JOIN d.member m WHERE m.mIdx = :m_idx AND d.delYN = :deleteYn")
    List<Dog> findByMIdxAndDeleteYn(@Param("m_idx") int m_idx, @Param("deleteYn") String deleteYn);
}
