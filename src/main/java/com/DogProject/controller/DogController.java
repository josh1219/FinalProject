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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
            File saveFile = new File();
            saveFile.setFType(3);
            saveFile.setTIdx(d_idx);
            fileService.saveFile(saveFile,image);


            redirectAttributes.addFlashAttribute("successMessage", "강아지 등록 성공!");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            log.warn("강아지 등록 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/insert";
        } catch (Exception e) {
            log.error("강아지 등록 중 예기치 못한 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return "redirect:/dog/insert";
        }
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
            if (!dog.getMember().getMIdx().equals(member.getMIdx())) {
                throw new AccessDeniedException("해당 강아지를 수정할 권한이 없습니다.");
            }

            model.addAttribute("dog", dog);
            model.addAttribute("member", member);
            model.addAttribute("file", fileService.getFileByTIdx(dIdx));
            return "dog/updateDog";

        } catch (Exception e) {
            log.error("강아지 수정 폼 로딩 중 오류 발생", e);
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
            if (!existingDog.getMember().getMIdx().equals(member.getMIdx())) {
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

            redirectAttributes.addFlashAttribute("successMessage", "강아지 정보가 수정되었습니다.");
            return "redirect:/";

        } catch (IllegalArgumentException | AccessDeniedException e) {
            log.warn("강아지 수정 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dog/update/" + dIdx;
        } catch (Exception e) {
            log.error("강아지 수정 중 예기치 못한 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return "redirect:/dog/update/" + dIdx;
        }
    }

    private void validateDogInput(String name, int age, String birthday, 
                                   String gender, String dType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상이어야 합니다.");
        }
        if (birthday == null || birthday.trim().isEmpty()) {
            throw new IllegalArgumentException("생일은 필수입니다.");
        }
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("성별은 필수입니다.");
        }
        if (dType == null || dType.trim().isEmpty()) {
            throw new IllegalArgumentException("종류는 필수입니다.");
        }
    }

    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    private File processImageFile(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;

        // 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(image.getInputStream(), filePath);

        // 파일 엔티티 생성
        File imageFile = new File();
        imageFile.setFType(3); // 강아지 이미지 타입
        imageFile.setFileRealName(originalFilename);
        imageFile.setFileSaveName(savedFileName);

        return imageFile;
    }
}
