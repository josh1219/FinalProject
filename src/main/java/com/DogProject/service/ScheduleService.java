package com.DogProject.service;

import com.DogProject.entity.Schedule;
import com.DogProject.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleService {

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
    public Schedule getScheduleById(int saIdx) {
        return scheduleRepository.findById(saIdx).orElse(null);
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
