package com.DogProject.service;

import com.DogProject.entity.Dog;
import com.DogProject.entity.File;
import com.DogProject.entity.Member;
import com.DogProject.repository.DogRepository;
import com.DogProject.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DogService {

    private final DogRepository dogRepository;
    private final FileService fileService ;
    private final FileRepository fileRepository ;

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

    public List<Dog> findAllByMember(Member member) {
        return dogRepository.findAllByMember(member);
    }

    public List<Dog> getDogsByMember(Member member) {
        return dogRepository.findAllByMemberAndDelYN(member, "N");
    }

    public List<Dog> getDeletedDogsByMember(Member member) {
        return dogRepository.findAllByMemberAndDelYN(member, "Y");
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

    public void restoreDog(int dIdx) {
        Dog dog = dogRepository.findBydIdx(dIdx)
                .orElseThrow(() -> new RuntimeException("Dog not found with id: " + dIdx));
        dog.setDelYN("N");
        dogRepository.save(dog);
    }
}
