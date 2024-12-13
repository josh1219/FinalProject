package com.DogProject.controller;

import com.DogProject.dto.MemberDTO;
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

@Controller
@RequestMapping("/walk")
public class WalkController {

    private final WalkSessionRepository walkSessionRepository;
    private final PathRepository pathRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

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
        Object mIdxObj = session.getAttribute("mIdx");
        if (mIdxObj == null) {
            return "redirect:/member/login";
        }

        int mIdx = Integer.parseInt(mIdxObj.toString());
        Member member = memberService.findByMIdx(mIdx);
        if (member == null) {
            session.invalidate();
            return "redirect:/member/login";
        }

        model.addAttribute("member", member);
        model.addAttribute("currentPoint", member.getPoint());
        return "walk/walk";
    }

    @PostMapping("/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveWalkData(
            @RequestParam("totalDistance") double totalDistance,
            @RequestParam("latitudes[]") List<Double> latitudes,
            @RequestParam("longitudes[]") List<Double> longitudes,
            HttpSession session) {
        try {
            Object mIdxObj = session.getAttribute("mIdx");
            if (mIdxObj == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            int mIdx = Integer.parseInt(mIdxObj.toString());
            Member member = memberService.findByMIdx(mIdx);
            if (member == null) {
                return ResponseEntity.badRequest().body("회원 정보를 찾을 수 없습니다.");
            }

            // 유효성 검사
            if (totalDistance < 0 || latitudes.size() != longitudes.size() || latitudes.isEmpty()) {
                return ResponseEntity.badRequest().body("잘못된 산책 데이터입니다.");
            }

            // WalkSession 생성 및 저장
            WalkSession walkSession = new WalkSession();
            walkSession.setMember(member);
            walkSession.setTotalDistance(totalDistance);
            walkSession.setWalkDate(LocalDateTime.now());
            walkSession.setWalkEndDate(LocalDateTime.now()); // 종료 시간 설정
            walkSession = walkSessionRepository.save(walkSession);

            // Path 데이터 일괄 저장
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < latitudes.size(); i++) {
                Path path = new Path();
                path.setMember(member);
                path.setWalkSession(walkSession);
                path.setLatitude(latitudes.get(i));
                path.setLongitude(longitudes.get(i));
                path.setSequence(i);
                path.setCreateTime(now);
                path.setUpdateTime(now);
                pathRepository.save(path);
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
            response.put("wsIdx", walkSession.getWsIdx());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("산책 데이터 저장에 실패했습니다: " + e.getMessage());
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
        try {
            Object mIdxObj = session.getAttribute("mIdx");
            if (mIdxObj == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

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
                walkSession.setWalkDate(LocalDateTime.now());
            } else {
                walkSession = walkSessionRepository.findById(wsIdx)
                    .orElseGet(() -> {
                        WalkSession newSession = new WalkSession();
                        newSession.setMember(member);
                        newSession.setWalkDate(LocalDateTime.now());
                        return newSession;
                    });
                walkSession.setTotalDistance(distance);
            }
            walkSession = walkSessionRepository.save(walkSession);

            // Path 데이터 저장
            LocalDateTime now = LocalDateTime.now();
            Path path = new Path();
            path.setMember(member);
            path.setWalkSession(walkSession);
            path.setLatitude(latitude);
            path.setLongitude(longitude);
            path.setSequence((int)pathRepository.countByWalkSession(walkSession).longValue());
            path.setCreateTime(now);
            path.setUpdateTime(now);
            pathRepository.save(path);

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
