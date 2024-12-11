package com.DogProject.repository;

import com.DogProject.entity.Dog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DogRepository extends JpaRepository<Dog, Integer> {
    Optional<Dog> findBydIdx(int d_idx);
}
