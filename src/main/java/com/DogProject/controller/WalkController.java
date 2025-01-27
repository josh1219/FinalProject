package com.DogProject.controller;

import com.DogProject.dto.MemberDTO;
import com.DogProject.dto.WalkSessionDTO;
import com.DogProject.entity.Member;
import com.DogProject.entity.Path;
import com.DogProject.entity.WalkSession;
import com.DogProject.service.MemberService;
import com.DogProject.repository.WalkSessionRepository;
import com.DogProject.repository.PathRepository;
import com.DogProject.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/walk")
public class WalkController {

    private final WalkSessionRepository walkSessionRepository;
    private final PathRepository pathRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Value("${google.maps.key}")
    private String googleMapsKey;

    public WalkController(WalkSessionRepository walkSessionRepository,
                         PathRepository pathRepository,
                         MemberService memberService,
                         MemberRepository memberRepository) {
        this.walkSessionRepository = walkSessionRepository;
        this.pathRepository = pathRepository;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("")
    public String walk(Model model, HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("mIdx") == null) {
            return "redirect:/member/login";
        }

        Object mIdxObj = session.getAttribute("mIdx");
        int mIdx = Integer.parseInt(mIdxObj.toString());
        Member member = memberService.findByMIdx(mIdx);
        if (member == null) {
            session.invalidate();
            return "redirect:/member/login";
        }

        model.addAttribute("member", member);
        model.addAttribute("currentPoint", member.getPoint());
        model.addAttribute("googleMapsKey", googleMapsKey);
        return "walk/walk";
    }

    @PostMapping("/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveWalkData(
            @RequestParam("totalDistance") double totalDistance,
            @RequestParam("wsIdx") int wsIdx,
            HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("mIdx") == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            Object mIdxObj = session.getAttribute("mIdx");
            int mIdx = Integer.parseInt(mIdxObj.toString());
            Member member = memberService.findByMIdx(mIdx);
            if (member == null) {
                return ResponseEntity.badRequest().body("회원 정보를 찾을 수 없습니다.");
            }

            // 기존 WalkSession 조회
            WalkSession walkSession = walkSessionRepository.findById(wsIdx)
                    .orElseThrow(() -> new RuntimeException("산책 세션을 찾을 수 없습니다."));

            // 마지막 Path의 create_time을 walk_end_date로 설정
            List<Path> paths = pathRepository.findPathsByWalkSessionOrderBySequence(wsIdx);
            if (!paths.isEmpty()) {
                walkSession.setWalkEndDate(paths.get(paths.size() - 1).getCreateTime());
                walkSession.setTotalDistance(totalDistance);
                walkSession = walkSessionRepository.save(walkSession);
            }

            // 포인트 적립 (100m당 1포인트, 최소 1포인트)
            int earnedPoints = Math.max(1, (int) (totalDistance / 100));
            member.setPoint(member.getPoint() + earnedPoints);
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "산책 데이터가 저장되었습니다.");
            response.put("earnedPoints", earnedPoints);
            response.put("currentPoint", member.getPoint());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("산책 데이터 저장에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getWalkList(HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("mIdx") == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            Object mIdxObj = session.getAttribute("mIdx");
            int mIdx = Integer.parseInt(mIdxObj.toString());
            List<WalkSession> walkSessions = walkSessionRepository.findByMember_mIdxOrderByWalkDateDesc(mIdx);
            
            List<WalkSessionDTO> walkSessionDTOs = walkSessions.stream()
                .map(WalkSessionDTO::fromEntity)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(walkSessionDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("산책 기록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/record")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> recordLocation(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("distance") double distance,
            @RequestParam(value = "wsIdx", required = false) Integer wsIdx,
            HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("mIdx") == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            Object mIdxObj = session.getAttribute("mIdx");
            int mIdx = Integer.parseInt(mIdxObj.toString());
            Member member = memberService.findByMIdx(mIdx);
            if (member == null) {
                return ResponseEntity.badRequest().body("회원 정보를 찾을 수 없습니다.");
            }

            // 유효성 검사
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180 || distance < 0) {
                return ResponseEntity.badRequest().body("잘못된 위치 데이터입니다.");
            }

            // WalkSession 처리
            WalkSession walkSession;
            if (wsIdx == null) {
                walkSession = new WalkSession();
                walkSession.setMember(member);
                walkSession.setTotalDistance(distance);
                walkSession = walkSessionRepository.save(walkSession);
            } else {
                walkSession = walkSessionRepository.findById(wsIdx)
                    .orElseGet(() -> {
                        WalkSession newSession = new WalkSession();
                        newSession.setMember(member);
                        newSession.setWalkDate(LocalDateTime.now());
                        newSession.setWalkEndDate(LocalDateTime.now()); // 새 세션의 시작 시간으로 초기화
                        return newSession;
                    });
                walkSession.setTotalDistance(distance);
                walkSession.setWalkEndDate(LocalDateTime.now()); // 현재 기록 시점으로 종료 시간 업데이트
            }
            walkSession = walkSessionRepository.save(walkSession);

            // Path 데이터 저장
            LocalDateTime now = LocalDateTime.now();
            Path path = new Path();
            path.setMember(member);
            path.setWalkSession(walkSession);
            path.setLatitude(latitude);
            path.setLongitude(longitude);
            
            // 마지막 시퀀스 값을 가져와서 1을 더함
            int lastSequence = pathRepository.findTopByWalkSessionOrderBySequenceDesc(walkSession)
                    .map(Path::getSequence)
                    .orElse(-1);
            path.setSequence(lastSequence + 1);
            
            path.setCreateTime(now);
            path.setUpdateTime(now);
            pathRepository.save(path);

            // 첫 번째와 마지막 Path의 create_time으로 walk_date와 walk_end_date 업데이트
            List<Path> paths = pathRepository.findPathsByWalkSessionOrderBySequence(walkSession.getWsIdx());
            if (!paths.isEmpty()) {
                walkSession.setWalkDate(paths.get(0).getCreateTime());
                walkSession.setWalkEndDate(paths.get(paths.size() - 1).getCreateTime());
                walkSession = walkSessionRepository.save(walkSession);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("wsIdx", walkSession.getWsIdx());
            response.put("message", "위치가 기록되었습니다.");
            response.put("currentDistance", distance);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
