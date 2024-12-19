package com.DogProject.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.DogProject.entity.Dog;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.service.DogService;
import com.DogProject.service.FileService;
import com.DogProject.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/dog")
@RequiredArgsConstructor
@Slf4j
public class DogController {

    private final DogService dogService;
    private final FileService fileService;
    private final MemberService memberService;

    @Value("${file.upload.directory}")
    private String uploadDir;

    private Member getMemberFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return memberService.findBymEmail(((UserDetails) principal).getUsername());
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = null;
            String provider = null;
            String socialId = null;
            
            // Google 로그인
            if (oauth2User.getAttributes().containsKey("email")) {
                email = oauth2User.getAttribute("email");
                provider = "google";
                socialId = oauth2User.getAttribute("sub");
            }
            // Naver 로그인
            else if (oauth2User.getAttributes().containsKey("response")) {
                Map<String, Object> response = (Map<String, Object>) oauth2User.getAttribute("response");
                email = (String) response.get("email");
                provider = "naver";
                socialId = String.valueOf(response.get("id"));
            }
            // Kakao 로그인
            else if (oauth2User.getAttributes().containsKey("kakao_account")) {
                provider = "kakao";
                Object id = oauth2User.getAttribute("id");
                socialId = id != null ? id.toString() : null;
                
                if (socialId != null) {
                    return memberService.findByProviderAndSocialId(provider, socialId);
                }
            }
            
            if (email != null) {
                return memberService.findBymEmail(email);
            } else if (provider != null && socialId != null) {
                return memberService.findByProviderAndSocialId(provider, socialId);
            }
        }
        return null;
    }

    @GetMapping("/insert")
    public String insertDogForm(Model model, @AuthenticationPrincipal Object principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }
            model.addAttribute("member", member);
            return "dog/insertDog";
        } catch (Exception e) {
            log.error("Error in insertDogForm", e);
            return "redirect:/";
        }
    }

    @PostMapping("/insert")
    public String insertDog(
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestParam("birthday") String birthday,
            @RequestParam("gender") String gender,
            @RequestParam("subGender") String subGender,
            @RequestParam("dType") String dType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            validateDogInput(name, age, birthday, gender, dType);
            if (image != null && !image.isEmpty()) {
                validateImageFile(image);
            }

            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }

            Dog dog = Dog.builder()
                    .name(name)
                    .age(age)
                    .birthday(birthday)
                    .gender(gender)
                    .subGender(subGender)
                    .dType(dType)
                    .member(member)
                    .build();

            Dog savedDog = dogService.saveDog(dog);
            int d_idx = savedDog.getDIdx();
            
            if (image != null && !image.isEmpty()) {
                try {
                    File saveFile = new File();
                    saveFile.setFType(3);
                    saveFile.setTIdx(d_idx);
                    fileService.saveFile(saveFile, image);
                } catch (IOException e) {
                    log.error("Error saving file: {}", e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "강아지가 등록되었습니다!");
            return "redirect:/dog/list";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/insert";
        } catch (Exception e) {
            log.error("Error in insertDog", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return "redirect:/dog/insert";
        }
    }

    @GetMapping("/list")
    public String dogList(Model model, @AuthenticationPrincipal Object principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }

            List<Dog> dogs = dogService.getDogsByMember(member);
            Map<Integer, File> dogImages = new HashMap<>();
            for(Dog dog : dogs){
                fileService.findByTypeAndIdx(3, dog.getDIdx())
                        .ifPresent(file -> dogImages.put(dog.getDIdx(), file));
            }
            model.addAttribute("dogs", dogs);
            model.addAttribute("dogImages", dogImages);
            model.addAttribute("member", member);
            return "dog/dogList";
        } catch (Exception e) {
            log.error("Error in dogList", e);
            return "redirect:/";
        }
    }

    @GetMapping("/deleted/list")
    public String deletedDogList(Model model, @AuthenticationPrincipal Object principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }

            List<Dog> deletedDogs = dogService.getDeletedDogsByMember(member);
            Map<Integer, File> dogImages = new HashMap<>();
            for(Dog dog : deletedDogs){
                fileService.findByTypeAndIdx(3, dog.getDIdx())
                        .ifPresent(file -> dogImages.put(dog.getDIdx(), file));
            }
            model.addAttribute("dogs", deletedDogs);
            model.addAttribute("dogImages", dogImages);
            return "dog/deletedDogList";
        } catch (Exception e) {
            log.error("Error in deletedDogList", e);
            return "redirect:/";
        }
    }

    @GetMapping("/update/{dIdx}")
    public String updateDogForm(
            @PathVariable int dIdx, 
            Model model,
            @AuthenticationPrincipal Object principal
    ) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }
            
            Dog dog = dogService.getDogById(dIdx);
            if (dog.getMember().getMIdx() != member.getMIdx()) {
                throw new AccessDeniedException("해당 강아지를 수정할 권한이 없습니다.");
            }

            model.addAttribute("dog", dog);
            model.addAttribute("member", member);
            fileService.findByTypeAndIdx(3, dIdx)
                    .ifPresent(file -> model.addAttribute("file", file));
            return "dog/updateDog";

        } catch (Exception e) {
            log.error("Error in updateDogForm", e);
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
            @RequestParam("subGender") String subGender,
            @RequestParam("dType") String dType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            validateDogInput(name, age, birthday, gender, dType);
            if (image != null && !image.isEmpty()) {
                validateImageFile(image);
            }

            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return "redirect:/member/login";
            }
            
            Dog existingDog = dogService.getDogById(dIdx);
            if (existingDog.getMember().getMIdx() != member.getMIdx()) {
                throw new AccessDeniedException("해당 강아지를 수정할 권한이 없습니다.");
            }

            existingDog.setName(name);
            existingDog.setAge(age);
            existingDog.setBirthday(birthday);
            existingDog.setGender(gender);
            existingDog.setSubGender(subGender);
            existingDog.setDType(dType);

            dogService.updateDog(existingDog, image);

            redirectAttributes.addFlashAttribute("successMessage", "강아지 정보가 수정되었습니다.");
            return "redirect:/dog/list";

        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/update/" + dIdx;
        } catch (Exception e) {
            log.error("Error in updateDog", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return "redirect:/dog/update/" + dIdx;
        }
    }

    @PostMapping("/remove/{dIdx}")
    @ResponseBody
    public ResponseEntity<?> deleteDog(@PathVariable int dIdx, @AuthenticationPrincipal Object principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            Dog dog = dogService.getDogById(dIdx);
            if (dog.getMember().getMIdx() != member.getMIdx()) {
                return ResponseEntity.badRequest().body("해당 강아지를 삭제할 권한이 없습니다.");
            }

            dogService.softDeleteDog(dIdx);
            return ResponseEntity.ok().body("/dog/list");
        } catch (Exception e) {
            log.error("Error deleting dog", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/restore/{dIdx}")
    @ResponseBody
    public ResponseEntity<?> restoreDog(@PathVariable int dIdx, @AuthenticationPrincipal Object principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            if (member == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }

            Dog dog = dogService.getDogById(dIdx);
            if (dog.getMember().getMIdx() != member.getMIdx()) {
                return ResponseEntity.badRequest().body("해당 강아지를 복구할 권한이 없습니다.");
            }

            dogService.restoreDog(dIdx);
            return ResponseEntity.ok().body("/dog/list");
        } catch (Exception e) {
            log.error("Error restoring dog", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void validateDogInput(String name, int age, String birthday, String gender, String dType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("강아지 이름을 입력해주세요.");
        }
        if (age <= 0) {
            throw new IllegalArgumentException("올바른 나이를 입력해주세요.");
        }
        if (birthday == null || birthday.trim().isEmpty()) {
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        }
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("성별을 선택해주세요.");
        }
        if (dType == null || dType.trim().isEmpty()) {
            throw new IllegalArgumentException("견종을 입력해주세요.");
        }
    }

    private void validateImageFile(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }
}
