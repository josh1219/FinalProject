package com.DogProject.controller;

import com.DogProject.dto.CalendarEventDTO;
import com.DogProject.entity.Schedule;
import com.DogProject.entity.WalkSession;
import com.DogProject.entity.Path;
import com.DogProject.entity.Member;
import com.DogProject.service.ScheduleService;
import com.DogProject.service.WalkService;
import com.DogProject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private WalkService walkService;
    
    @Autowired
    private MemberService memberService;
    
    @GetMapping("")
    public String schedule(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Object mIdx = session.getAttribute("mIdx");
        
        if (mIdx == null) {
            return "redirect:/member/login";
        }
        
        model.addAttribute("mIdx", mIdx);
        return "schedule/schedule";
    }

    @GetMapping("/events")
    @ResponseBody
    public ResponseEntity<Map<String, List<CalendarEventDTO>>> getEvents(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("mIdx") == null) {
            return ResponseEntity.status(401).body(null);
        }
        
        List<CalendarEventDTO> events = new ArrayList<>();
        
        try {
            // 세션에서 사용자 정보 가져오기
            Object mIdxObj = session.getAttribute("mIdx");
            
            if (mIdxObj == null) {
                logger.warn("세션에서 사용자 정보를 찾을 수 없음");
                return ResponseEntity.ok(Map.of("events", new ArrayList<>()));
            }
            
            try {
                int mIdx = Integer.parseInt(mIdxObj.toString());
                logger.info("사용자 ID 확인됨: {}", mIdx);
                
                // 일정 데이터 가져오기
                List<Schedule> schedules = scheduleService.getSchedulesByMember(mIdx);
                logger.info("일정 데이터 조회: {} 건", schedules.size());
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
                List<WalkSession> walkSessions = walkService.getWalkSessionsByMember(mIdx);
                logger.info("산책 데이터 조회: {} 건", walkSessions.size());
                
                for (WalkSession walk : walkSessions) {
                    logger.info("산책 데이터 처리: wsIdx={}, walkDate={}, walkEndDate={}, distance={}", 
                        walk.getWsIdx(), walk.getWalkDate(), walk.getWalkEndDate(), walk.getTotalDistance());
                        
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
            } catch (NumberFormatException e) {
                logger.error("사용자 ID 파싱 실패: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("산책 데이터 처리 중 오류: {}", e.getMessage(), e);
            }
            
            // 시간순으로 정렬
            events.sort((a, b) -> a.getStart().compareTo(b.getStart()));
            logger.info("총 이벤트 수 (일정 + 산책): {}", events.size());
            
            Map<String, List<CalendarEventDTO>> response = new HashMap<>();
            response.put("events", events);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("이벤트 조회 중 오류 발생: " + e.getMessage(), e);
            Map<String, List<CalendarEventDTO>> errorResponse = new HashMap<>();
            errorResponse.put("events", new ArrayList<>());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/walk/{wsIdx}")
    @ResponseBody
    public ResponseEntity<?> getWalkPath(@PathVariable Integer wsIdx) {
        try {
            logger.info("산책 경로 조회 요청: wsIdx={}", wsIdx);
            
            WalkSession walkSession = walkService.getWalkSessionById(wsIdx);
            if (walkSession == null) {
                logger.warn("산책 세션을 찾을 수 없음: wsIdx={}", wsIdx);
                return ResponseEntity.notFound().build();
            }

            List<Path> pathList = walkService.getPathsByWalkSessionOrderBySequence(walkSession.getWsIdx());
            if (pathList.isEmpty()) {
                logger.warn("산책 경로 데이터가 없음: wsIdx={}", wsIdx);
                return ResponseEntity.ok(Map.of("path", List.of(), "distance", walkSession.getTotalDistance()));
            }
            
            logger.info("산책 경로 데이터 조회 성공: wsIdx={}, pathCount={}", wsIdx, pathList.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("path", pathList);
            response.put("distance", walkSession.getTotalDistance());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("산책 경로 조회 중 오류 발생: wsIdx=" + wsIdx, e);
            return ResponseEntity.internalServerError().body("산책 경로를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveSchedule(@RequestBody Map<String, Object> scheduleData, HttpServletRequest request) {
        try {
            // 세션에서 사용자 정보 가져오기
            HttpSession session = request.getSession();
            Object mIdxObj = session.getAttribute("mIdx");
            
            if (mIdxObj == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }
            
            try {
                int mIdx = Integer.parseInt(mIdxObj.toString());
                logger.info("사용자 ID 확인됨: {}", mIdx);
                
                // 회원 정보 조회
                Member member = memberService.findByMIdx(mIdx);
                if (member == null) {
                    return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
                }
                
                logger.info("일정 저장 요청: title={}, content={}, startTime={}, endTime={}, mIdx={}", 
                        scheduleData.get("title"), scheduleData.get("content"), 
                        scheduleData.get("startTime"), scheduleData.get("endTime"), mIdx);
                
                Schedule schedule = new Schedule();
                schedule.setTitle((String) scheduleData.get("title"));
                schedule.setContent((String) scheduleData.get("content"));
                
                // 시간대 정보가 포함된 문자열에서 LocalDateTime으로 변환
                String startTimeStr = (String) scheduleData.get("startTime");
                String endTimeStr = (String) scheduleData.get("endTime");
                
                ZonedDateTime startZdt = ZonedDateTime.parse(startTimeStr);
                ZonedDateTime endZdt = ZonedDateTime.parse(endTimeStr);
                
                // ZonedDateTime을 LocalDateTime으로 변환 (한국 시간대 기준)
                ZoneId koreaZoneId = ZoneId.of("Asia/Seoul");
                LocalDateTime startTime = startZdt.withZoneSameInstant(koreaZoneId).toLocalDateTime();
                LocalDateTime endTime = endZdt.withZoneSameInstant(koreaZoneId).toLocalDateTime();
                
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setMember(member);
                
                Schedule savedSchedule = scheduleService.saveSchedule(schedule);
                
                logger.info("일정 저장 성공: saIdx={}", savedSchedule.getSaIdx());
                
                CalendarEventDTO response = new CalendarEventDTO();
                response.setId("s" + savedSchedule.getSaIdx());
                response.setTitle(savedSchedule.getTitle());
                response.setContent(savedSchedule.getContent());
                response.setStart(savedSchedule.getStartTime());
                response.setEnd(savedSchedule.getEndTime());
                response.setType("schedule");
                
                return ResponseEntity.ok(response);
            } catch (NumberFormatException e) {
                logger.error("사용자 ID 파싱 실패: {}", e.getMessage());
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }
        } catch (Exception e) {
            logger.error("일정 저장 중 오류 발생: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body("일정 저장에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateSchedule(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String id = request.get("id").toString();
            logger.info("수신된 일정 ID: {}", id);
            
            int scheduleId = Integer.parseInt(id.substring(1));
            logger.info("변환된 일정 ID 숫자: {}", scheduleId);
            
            Schedule schedule = scheduleService.getScheduleById(scheduleId);
            logger.info("조회된 일정: {}", (schedule != null ? "찾음 (ID: " + schedule.getSaIdx() + ")" : "없음"));
            
            if (schedule == null) {
                response.put("success", false);
                response.put("message", "일정을 찾을 수 없습니다.");
                return response;
            }
            
            schedule.setTitle((String) request.get("title"));
            schedule.setContent((String) request.get("body"));
            
            String startStr = (String) request.get("start");
            String endStr = (String) request.get("end");
            
            // 한국 시간대 설정
            ZoneId koreaZoneId = ZoneId.of("Asia/Seoul");
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            
            // 시간 파싱 및 변환
            ZonedDateTime startZdt = ZonedDateTime.parse(startStr, formatter);
            ZonedDateTime endZdt = ZonedDateTime.parse(endStr, formatter);
            
            // ZonedDateTime을 LocalDateTime으로 변환
            LocalDateTime start = startZdt.withZoneSameInstant(koreaZoneId).toLocalDateTime();
            LocalDateTime end = endZdt.withZoneSameInstant(koreaZoneId).toLocalDateTime();
            
            schedule.setStartTime(start);
            schedule.setEndTime(end);
            
            logger.info("변환된 시작 시간: {}", start);
            logger.info("변환된 종료 시간: {}", end);
            
            scheduleService.updateSchedule(schedule);
            
            response.put("success", true);
            response.put("message", "일정이 성공적으로 수정되었습니다.");
            
            return response;
        } catch (Exception e) {
            logger.error("일정 수정 중 오류 발생: " + e.getMessage(), e);
            response.put("success", false);
            response.put("message", "일정 수정에 실패했습니다: " + e.getMessage());
            return response;
        }
    }
}
