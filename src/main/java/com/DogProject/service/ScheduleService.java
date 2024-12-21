package com.DogProject.service;

import com.DogProject.entity.Schedule;
import com.DogProject.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByMember(int mIdx) {
        logger.info("스케줄 조회 시작: mIdx={}", mIdx);
        List<Schedule> schedules = scheduleRepository.findByMember_mIdxOrderByStartTimeDesc(mIdx);
        logger.info("스케줄 조회 결과: {} 건", schedules.size());
        for (Schedule schedule : schedules) {
            logger.info("스케줄: saIdx={}, title={}, startTime={}, endTime={}", 
                schedule.getSaIdx(), schedule.getTitle(), schedule.getStartTime(), schedule.getEndTime());
        }
        return schedules;
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(int saIdx) {
        return scheduleRepository.findById(saIdx).orElse(null);
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
