package com.DogProject.repository;

import com.DogProject.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {

    @Query(value = "SELECT * FROM file WHERE t_idx = :tIdx", nativeQuery = true)
    Optional<File> findByIdx(@Param("tIdx") int tIdx);

    @Query(value = "SELECT * FROM file WHERE f_type = :fType AND t_idx = :tIdx", nativeQuery = true)
    Optional<File> findByTypeAndIdx(@Param("fType") int fType, @Param("tIdx") int tIdx);

    @Query(value = "SELECT * FROM file WHERE f_type = :fType AND t_idx = :tIdx", nativeQuery = true)
    List<File> findAllByTypeAndIdx(@Param("fType") int fType, @Param("tIdx") int tIdx);
    
}
