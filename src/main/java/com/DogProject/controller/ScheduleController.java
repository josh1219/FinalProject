package com.DogProject.controller;

import com.DogProject.dto.CalendarEventDTO;
import com.DogProject.entity.Schedule;
import com.DogProject.entity.WalkSession;
import com.DogProject.service.ScheduleService;
import com.DogProject.service.WalkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private WalkService walkService;
    
    @GetMapping("")
    public String schedule() {
        return "schedule/schedule";
    }

    @GetMapping("/events")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDTO>> getEvents() {
        List<CalendarEventDTO> events = new ArrayList<>();
        
        // 일정 데이터 가져오기
        List<Schedule> schedules = scheduleService.getAllSchedules();
        for (Schedule schedule : schedules) {
            CalendarEventDTO event = new CalendarEventDTO();
            event.setId("s" + schedule.getSaIdx());
            event.setTitle(schedule.getTitle());
            event.setContent(schedule.getContent());
            event.setStart(schedule.getStartTime());
            event.setEnd(schedule.getEndTime());
            event.setType("schedule");
            events.add(event);
        }
        
        // 산책 세션 데이터 가져오기
        List<WalkSession> walkSessions = walkService.getAllWalkSessions();
        for (WalkSession walk : walkSessions) {
            CalendarEventDTO event = new CalendarEventDTO();
            event.setId("w" + walk.getWsIdx());
            event.setTitle("산책");
            event.setContent("총 거리: " + String.format("%.2f", walk.getTotalDistance()) + "km");
            event.setStart(walk.getWalkDate());
            event.setEnd(walk.getWalkEndDate());
            event.setType("walk");
            event.setTotalDistance(walk.getTotalDistance());
            events.add(event);
        }
        
        return ResponseEntity.ok(events);
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveSchedule(@RequestBody Map<String, Object> scheduleData) {
        try {
            Schedule schedule = new Schedule();
            schedule.setTitle((String) scheduleData.get("title"));
            schedule.setContent((String) scheduleData.get("content"));
            
            LocalDateTime startTime = LocalDateTime.parse((String) scheduleData.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse((String) scheduleData.get("endTime"));
            
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            
            Schedule savedSchedule = scheduleService.saveSchedule(schedule);
            
            CalendarEventDTO response = new CalendarEventDTO();
            response.setId("s" + savedSchedule.getSaIdx());
            response.setTitle(savedSchedule.getTitle());
            response.setContent(savedSchedule.getContent());
            response.setStart(savedSchedule.getStartTime());
            response.setEnd(savedSchedule.getEndTime());
            response.setType("schedule");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일정 저장에 실패했습니다: " + e.getMessage());
        }
    }
}
