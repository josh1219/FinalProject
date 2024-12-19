package com.DogProject.repository;

import com.DogProject.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByMember_mIdxOrderByStartTimeDesc(int mIdx);
}
