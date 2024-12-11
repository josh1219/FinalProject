package com.DogProject.repository;

import com.DogProject.entity.File;

import static org.mockito.Answers.values;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {

    Optional<File> findByTIdx(int tIdx);
    
}
