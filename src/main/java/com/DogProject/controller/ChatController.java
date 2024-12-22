package com.DogProject.controller;

import com.DogProject.domain.ChatMessage;
import com.DogProject.entity.Chat;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.repository.MemberRepository;
import com.DogProject.service.ChatService;
import com.DogProject.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final FileService fileService;
    private final MemberRepository memberRepository;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessage handleMessage(@DestinationVariable String roomId, ChatMessage message) {
        // DB에 저장
        String[] ids = roomId.split("_");
        int senderId = message.getSenderId().intValue();
        int receiverId = Integer.parseInt(ids[0].equals(String.valueOf(message.getSenderId())) ? ids[1] : ids[0]);
        chatService.saveChat(message.getContent(), senderId, receiverId);
        return message;
    }

    @GetMapping("/chatList")
    public String chatList(Model model, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("sessionMember");
        if (loginUser == null) {
            return "redirect:/member/login";
        }
        
        List<Chat> latestChats = chatService.getLatestChatsByUserId(loginUser.getMIdx());
        model.addAttribute("latestChats", latestChats);
        model.addAttribute("currentUser", loginUser);
        
        return "chatList";
    }

    @GetMapping("/chat/room/{receiverId}")
    public String chatRoom(@PathVariable int receiverId, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("sessionMember");
        if (member == null) {
            return "redirect:/member/login";
        }

        try {
            List<Chat> chatHistory = chatService.getChatsBetweenUsers(member.getMIdx(), receiverId);
            model.addAttribute("currentUser", member);
            model.addAttribute("chatHistory", chatHistory);
            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "채팅 내역을 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    @GetMapping("/chat/{id1}_{id2}")
    public String chatRoom(@PathVariable int id1, @PathVariable int id2, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("sessionMember");
        
        if (member == null) {
            return "redirect:/member/login";
        }

        // 현재 사용자가 채팅방의 참여자인지 확인
        if (member.getMIdx() != id1 && member.getMIdx() != id2) {
            return "redirect:/error";
        }

        // 자기 자신과의 채팅 방지
        if (id1 == id2) {
            return "redirect:/error";
        }

        // 채팅방 ID 정규화 (항상 작은 ID가 앞에 오도록)
        String roomId = id1 < id2 ? id1 + "_" + id2 : id2 + "_" + id1;
        if (!roomId.equals(id1 + "_" + id2)) {
            return "redirect:/chat/" + roomId;
        }

        // 이전 채팅 내역 조회
        List<Chat> chatHistory = chatService.getChatsBetweenUsers(id1, id2);

        // 현재 날짜 정보
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekdays = {"일", "월", "화", "수", "목", "금", "토"};
        Map<String, String> datesToWeekdays = new HashMap<>();
        
        // 오늘 날짜의 요일 정보 추가
        Calendar today = Calendar.getInstance();
        String todayStr = sdf.format(today.getTime());
        datesToWeekdays.put(todayStr, weekdays[today.get(Calendar.DAY_OF_WEEK) - 1]);
        
        // 채팅 기록이 있는 경우
        if (!chatHistory.isEmpty()) {
            // 채팅 기록의 모든 날짜에 대한 요일 정보 추가
            for (Chat chat : chatHistory) {
                try {
                    String dateStr = chat.getSendTime().substring(0, 10);
                    if (!datesToWeekdays.containsKey(dateStr)) {
                        Date date = sdf.parse(dateStr);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        datesToWeekdays.put(dateStr, weekdays[cal.get(Calendar.DAY_OF_WEEK) - 1]);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // 현재 사용자 정보를 Map으로 변환
        Map<String, Object> currentMember = new HashMap<>();
        currentMember.put("mIdx", member.getMIdx());
        currentMember.put("mName", member.getName());

        model.addAttribute("currentMember", currentMember);
        model.addAttribute("roomId", roomId);
        model.addAttribute("chatHistory", chatHistory);
        model.addAttribute("datesToWeekdays", datesToWeekdays);
        model.addAttribute("today", todayStr);
        
        return "chat";
    }

    @PostMapping("/chat/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, 
                                      @RequestParam("roomId") String roomId,
                                      @RequestParam(value = "message", required = false) String message,
                                      HttpSession session) {
        try {
            Member member = (Member) session.getAttribute("sessionMember");
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            log.info("파일 업로드 시작: {}, roomId: {}", file.getOriginalFilename(), roomId);

            String[] ids = roomId.split("_");
            int chatRoomId = Integer.parseInt(ids[0]);

            File savedFile = fileService.saveFile(file, 4, chatRoomId);
            log.info("파일 저장 완료: {}", savedFile.getFileSaveName());

            String fileType = file.getContentType();
            String filePath = "/uploads/" + savedFile.getFileSaveName();
            String content;
            
            if (fileType != null && fileType.startsWith("image/")) {
                content = filePath;
            } else {
                String extension = savedFile.getFileRealName().substring(savedFile.getFileRealName().lastIndexOf(".") + 1).toLowerCase();
                String fileIcon = getFileIcon(extension);
                content = filePath + "|" + savedFile.getFileRealName() + "|" + fileIcon;
            }

            // 텍스트 메시지가 있으면 추가
            if (message != null && !message.trim().isEmpty()) {
                content += "|TEXT|" + message.trim();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "파일 업로드 성공");
            response.put("content", content);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String getFileIcon(String extension) {
        switch (extension) {
            case "pdf":
                return "far fa-file-pdf";
            case "doc":
            case "docx":
                return "far fa-file-word";
            case "xls":
            case "xlsx":
                return "far fa-file-excel";
            case "zip":
            case "rar":
                return "far fa-file-archive";
            default:
                return "far fa-file";
        }
    }
}