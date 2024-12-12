package com.DogProject.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.DogProject.entity.Dog;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.service.DogService;
import com.DogProject.service.FileService;
import com.DogProject.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/dog")
@RequiredArgsConstructor
@Slf4j
public class DogController {

    private final DogService dogService;
    private final MemberService memberService;
    private final FileService fileService;

    @Value("${file.upload.directory}")
    private String uploadDir;

    @GetMapping("/insert")
    public String insertDogForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 회원 정보 전달
        Member member = memberService.findBymEmail(userDetails.getUsername());
        model.addAttribute("member", member);
        return "dog/insertDog";
    }

    @PostMapping("/insert")
    public String insertDog(
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestParam("birthday") String birthday,
            @RequestParam("gender") String gender,
            @RequestParam("dType") String dType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 입력값 검증
            validateDogInput(name, age, birthday, gender, dType);
            if (image != null && !image.isEmpty()) {
                validateImageFile(image);
            }

            // 현재 로그인한 회원 조회
            Member member = memberService.findBymEmail(userDetails.getUsername());

            // Dog 엔티티 생성
            Dog dog = Dog.builder()
                    .name(name)
                    .age(age)
                    .birthday(birthday)
                    .gender(gender)
                    .dType(dType)
                    .member(member)
                    .build();

            // 강아지와 이미지 함께 저장
            Dog savedDog = dogService.saveDog(dog);
            int d_idx = savedDog.getDIdx();
            
            log.info("Dog saved successfully. dIdx: {}", d_idx);
            
            // 이미지 파일이 존재하는 경우에만 파일 저장 처리
            if (image != null && !image.isEmpty()) {
                log.info("Image file exists. Filename: {}, Size: {}", image.getOriginalFilename(), image.getSize());
                try {
                    File saveFile = new File();
                    saveFile.setFType(3);
                    saveFile.setTIdx(d_idx);
                    log.info("Attempting to save file. dIdx: {}, fType: {}", d_idx, saveFile.getFType());
                    
                    File savedFile = fileService.saveFile(saveFile, image);
                    
                    if (savedFile == null) {
                        log.warn("File save failed: dIdx={}", d_idx);
                    } else {
                        log.info("File saved successfully: dIdx={}, fileName={}, savedFileName={}", 
                            d_idx, savedFile.getFileRealName(), savedFile.getFileSaveName());
                    }
                } catch (IOException e) {
                    log.error("Error occurred while saving file: {}", e.getMessage(), e);
                    // 파일 저장 실패해도 강아지 등록은 완료된 것으로 처리
                }
            } else {
                log.info("No image file or empty file");
            }

            redirectAttributes.addFlashAttribute("successMessage", "Dog registration successful!");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            log.warn("Dog registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/insert";
        } catch (Exception e) {
            log.error("An unexpected error occurred during dog registration", e);
            redirectAttributes.addFlashAttribute("errorMessage", "A server error occurred.");
            return "redirect:/dog/insert";
        }
    }

    @GetMapping("/list")
    public String dogList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.findBymEmail(userDetails.getUsername());
        List<Dog> dogs = dogService.findAllByMember(member);
        
        // 각 강아지의 이미지 정보를 맵으로 저장
        Map<Integer, File> dogImages = new HashMap<>();
        for (Dog dog : dogs) {
            fileService.findByTypeAndIdx(3, dog.getDIdx())
                      .ifPresent(file -> dogImages.put(dog.getDIdx(), file));
        }
        
        model.addAttribute("dogs", dogs);
        model.addAttribute("dogImages", dogImages);
        model.addAttribute("member", member);
        return "dog/dogList";
    }

    @GetMapping("/update/{dIdx}")
    public String updateDogForm(
            @PathVariable int dIdx, 
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            // 현재 로그인한 회원 정보
            Member member = memberService.findBymEmail(userDetails.getUsername());
            
            // 강아지 조회
            Dog dog = dogService.getDogById(dIdx);
            
            // 현재 로그인한 회원의 강아지인지 확인
            if (dog.getMember().getMIdx() != member.getMIdx()) {
                throw new AccessDeniedException("해당 강아지를 수정할 권한이 없습니다.");
            }            

            model.addAttribute("dog", dog);
            model.addAttribute("member", member);
            fileService.findByTypeAndIdx(3, dIdx)
                      .ifPresent(file -> model.addAttribute("file", file));
            return "dog/updateDog";

        } catch (Exception e) {
            log.error("An error occurred while loading the dog update form", e);
            return "redirect:/";
        }
    }

    @PostMapping("/update/{dIdx}")
    public String updateDog(
            @PathVariable int dIdx,
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestParam("birthday") String birthday,
            @RequestParam("gender") String gender,
            @RequestParam("dType") String dType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 입력값 검증
            validateDogInput(name, age, birthday, gender, dType);
            if (image != null && !image.isEmpty()) {
                validateImageFile(image);
            }

            Member member = memberService.findBymEmail(userDetails.getUsername());
            
            // 기존 강아지 조회
            Dog existingDog = dogService.getDogById(dIdx);

            // 현재 로그인한 회원의 강아지인지 확인
            if (existingDog.getMember().getMIdx() != member.getMIdx()) {
                throw new AccessDeniedException("해당 강아지의 정보를 수정할 권한이 없습니다.");

            }

            // 강아지 정보 업데이트
            existingDog.setName(name);
            existingDog.setAge(age);
            existingDog.setBirthday(birthday);
            existingDog.setGender(gender);
            existingDog.setDType(dType);

            // 강아지와 이미지 함께 업데이트
            dogService.updateDog(existingDog, image);

            redirectAttributes.addFlashAttribute("successMessage", "Dog information has been updated.");
            return "redirect:/";

        } catch (IllegalArgumentException | AccessDeniedException e) {
            log.warn("Dog update failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/update/" + dIdx;
        } catch (Exception e) {
            log.error("An unexpected error occurred during dog update", e);
            redirectAttributes.addFlashAttribute("errorMessage", "A server error occurred.");
            return "redirect:/dog/update/" + dIdx;
        }
    }

    @PostMapping("/remove/{dIdx}")
    @ResponseBody
    public ResponseEntity<?> deleteDog(@PathVariable int dIdx) {
        try {
            log.info("Deleting dog with ID: {}", dIdx);
            dogService.softDeleteDog(dIdx);
            log.info("Successfully deleted dog with ID: {}", dIdx);
            return ResponseEntity.ok().body("/dog/deleted/list");
        } catch (Exception e) {
            log.error("Error deleting dog with ID: {}", dIdx, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/deleted/list")
    public String deletedDogList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberService.findBymEmail(userDetails.getUsername());
            List<Dog> deletedDogs = dogService.getDeletedDogsByMember(member);
            model.addAttribute("dogs", deletedDogs);
            model.addAttribute("member", member);
            return "dog/deletedDogList";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/restore/{dIdx}")
    @ResponseBody
    public ResponseEntity<?> restoreDog(@PathVariable int dIdx) {
        try {
            dogService.restoreDog(dIdx);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void validateDogInput(String name, int age, String birthday, 
                                   String gender, String dType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age must be 0 or greater.");
        }
        if (birthday == null || birthday.trim().isEmpty()) {
            throw new IllegalArgumentException("Birthday is required.");
        }
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (dType == null || dType.trim().isEmpty()) {
            throw new IllegalArgumentException("Type is required.");
        }
    }

    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files can be uploaded.");
        }
    }
}
