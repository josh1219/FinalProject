package com.DogProject.controller;

import com.DogProject.entity.Member;
import com.DogProject.entity.Path;
import com.DogProject.entity.WalkSession;
import com.DogProject.repository.MemberRepository;
import com.DogProject.repository.PathRepository;
import com.DogProject.repository.WalkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/walk")
public class WalkApiController {

    private final WalkSessionRepository walkSessionRepository;
    private final MemberRepository memberRepository;
    private final PathRepository pathRepository;

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startWalk(
    @RequestParam int mIdx) {
        try {
            Member member = memberRepository.findById(mIdx)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            WalkSession walkSession = new WalkSession();
            walkSession.setMember(member);
            walkSession.setTotalDistance(0.0);
            walkSession.setWalkDate(LocalDateTime.now());
            
            WalkSession savedWalkSession = walkSessionRepository.save(walkSession);
            walkSessionRepository.flush();
            System.out.println("wsIdx : " +savedWalkSession.getWsIdx());
            
            return ResponseEntity.ok(Map.of("wsIdx", String.valueOf(savedWalkSession.getWsIdx())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopWalk(
            @RequestParam int wsIdx,
            @RequestParam Double totalDistance) {
        
        try {
            WalkSession walkSession = walkSessionRepository.findById(wsIdx)
                    .orElseThrow(() -> new RuntimeException("Walk session not found"));
            
            walkSession.setTotalDistance(totalDistance);
            walkSessionRepository.save(walkSession);

            int earnedPoint = (int)(totalDistance * 0.01);
            
            Member member = walkSession.getMember();
            member.setPoint(member.getPoint() + earnedPoint);
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("wsIdx", wsIdx);
            response.put("totalDistance", totalDistance);
            response.put("earnedPoint", earnedPoint);
            response.put("totalPoint", member.getPoint());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Walk session stop failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/record")
    public ResponseEntity<?> recordPath(
            @RequestParam int wsIdx,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double distance) {
        
        try {
            WalkSession walkSession = walkSessionRepository.findById(wsIdx)
                    .orElseThrow(() -> new RuntimeException("Walk session not found"));
            
            // Path 저장
            Path path = new Path();
            path.setWalkSession(walkSession);
            path.setMember(walkSession.getMember());
            path.setLatitude(latitude);
            path.setLongitude(longitude);
            Long pathCount = pathRepository.countByWalkSession(walkSession);
            path.setSequence(pathCount != null ? pathCount.intValue() : 0);
            LocalDateTime now = LocalDateTime.now();
            path.setCreateTime(now);
            path.setUpdateTime(now);  
            pathRepository.save(path);
            
            walkSession.setTotalDistance(distance);
            walkSessionRepository.save(walkSession);

            Map<String, Object> response = new HashMap<>();
            response.put("wsIdx", wsIdx);
            response.put("distance", distance);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Path recording failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/history/{wsIdx}")
    public ResponseEntity<List<Path>> getWalkHistory(@PathVariable int wsIdx) {
        try {
            List<Path> paths = pathRepository.findPathsByWalkSessionOrderBySequence(wsIdx);
            return ResponseEntity.ok(paths);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentWalks(
            @RequestParam int mIdx) {
        try {
            List<WalkSession> recentWalks = walkSessionRepository.findByMember_mIdxOrderByWalkDateDesc(mIdx);
            
            List<Map<String, Object>> response = recentWalks.stream()
                    .map(walk -> {
                        Map<String, Object> walkData = new HashMap<>();
                        walkData.put("wsIdx", walk.getWsIdx());
                        walkData.put("walkDate", walk.getWalkDate());
                        walkData.put("totalDistance", walk.getTotalDistance());
                        return walkData;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
