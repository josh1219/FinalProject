package com.DogProject.service;

import com.DogProject.entity.Dog;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.repository.DogRepository;
import com.DogProject.repository.FileRepository;
import com.DogProject.service.FileService;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DogService {

    @Autowired
    private DogRepository dogRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Transactional
    public Dog saveDog(Dog dog) throws Exception {
        try {
            Dog savedDog = dogRepository.save(dog);
            log.debug("강아지 저장 완료 - ID: {}", savedDog.getDIdx());
            return savedDog;

        } catch (Exception e) {
            log.error("강아지 저장 실패", e);
            throw e;
        }
    }

    @Transactional
    public Dog updateDog(Dog dog, MultipartFile image) throws Exception {
        log.debug("강아지 정보 수정 시작 - ID: {}", dog.getDIdx());
        try {
            Dog updatedDog = dogRepository.save(dog);
            
            // 새 이미지가 있는 경우
            if (image != null && !image.isEmpty()) {
                // 기존 이미지 삭제
                fileService.findByTypeAndIdx(3, dog.getDIdx())
                    .ifPresent(existingFile -> fileService.deleteFile(existingFile));
                
                // 새 이미지 저장
                File saveFile = new File();
                saveFile.setFType(3);
                saveFile.setTIdx(dog.getDIdx());
                fileService.saveFile(saveFile, image);
            }
            
            log.debug("강아지 정보 수정 완료 - ID: {}", updatedDog.getDIdx());
            return updatedDog;
        } catch (Exception e) {
            log.error("강아지 정보 수정 실패", e);
            throw e;
        }
    }

    public Dog getDogById(int dIdx) {
        log.debug("강아지 조회 - ID: {}", dIdx);
        return dogRepository.findById(dIdx)
                .orElseThrow(() -> {
                    log.warn("강아지를 찾을 수 없음 - ID: {}", dIdx);
                    return new RuntimeException("강아지를 찾을 수 없습니다.");
                });
    }

    public List<Dog> getDogsByMember(Member member) {
        return dogRepository.findAllByMemberAndDelYN(member, "N");
    }

    public List<Dog> getDeletedDogsByMember(Member member) {
        return dogRepository.findAllByMemberAndDelYN(member, "Y");
    }

    public List<Dog> getDogsByMemberIdx(int m_idx) {
        return dogRepository.findByMIdxAndDeleteYn(m_idx, "N");
    }

    @Transactional
    public void softDeleteDog(int dIdx) {
        Dog dog = dogRepository.findBydIdx(dIdx)
                .orElseThrow(() -> new RuntimeException("Dog not found with id: " + dIdx));
        log.info("Found dog: {}", dog);
        dog.setDelYN("Y");
        Dog savedDog = dogRepository.save(dog);
        log.info("Updated dog delYN to Y: {}", savedDog);
    }

    @Transactional
    public void restoreDog(int dIdx) {
        Dog dog = dogRepository.findBydIdx(dIdx)
                .orElseThrow(() -> new RuntimeException("Dog not found with id: " + dIdx));
        log.info("Found dog to restore: {}", dog);
        dog.setDelYN("N");
        Dog savedDog = dogRepository.save(dog);
        log.info("Updated dog delYN to N: {}", savedDog);
    }
}
