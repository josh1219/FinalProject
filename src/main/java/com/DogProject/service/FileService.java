package com.DogProject.service;

import com.DogProject.entity.Dog;
import com.DogProject.entity.File;
import com.DogProject.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource(uploadDirectory);
            Path uploadPath = Paths.get(resource.getFile().getAbsolutePath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Transactional
    public File saveFile(File file, MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(uploadDirectory);
        Path filePath = uploadPath.resolve(savedFileName);
        
        log.debug("Saving file: {} to path: {}", originalFilename, filePath);

        try {
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            file.setFileRealName(originalFilename);
            file.setFileSaveName(savedFileName);
            
            return fileRepository.save(file);
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public File saveFile(MultipartFile multipartFile, int fType, int tIdx) {
        try {
            log.info("파일 저장 시작: {}, type: {}, tIdx: {}", multipartFile.getOriginalFilename(), fType, tIdx);
            
            // File 엔티티 생성
            File file = new File();
            file.setFType(fType);
            file.setTIdx(tIdx);
            
            // 실제 파일 저장
            return saveFile(file, multipartFile);
            
        } catch (Exception e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteFile(File file) {
        if (file != null) {
            try {
                Resource resource = resourceLoader.getResource(uploadDirectory);
                Path uploadPath = Paths.get(resource.getFile().getAbsolutePath());
                Path filePath = uploadPath.resolve(file.getFileSaveName());
                Files.deleteIfExists(filePath);
                fileRepository.delete(file);
            } catch (IOException e) {
                log.error("파일 삭제 실패: {}", e.getMessage());
                throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
            }
        }
    }

    @Transactional
    public void deleteFile(int fIdx) {
        File file = fileRepository.findById(fIdx)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        // 실제 파일 삭제
        try {
            Resource resource = resourceLoader.getResource(uploadDirectory);
            Path filePath = Paths.get(resource.getFile().getAbsolutePath(), file.getFileSaveName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }

        // DB에서 파일 정보 삭제
        fileRepository.delete(file);
    }

    public String getFilePath(String fileSaveName) {
        return "/uploads/" + fileSaveName;
    }

    @Transactional(readOnly = true)
    public Optional<File> findByTypeAndIdx(int fType, int tIdx) {
        return fileRepository.findByTypeAndIdx(fType, tIdx);
    }

    @Transactional(readOnly = true)
    public List<File> findAllByTypeAndIdx(int fType, int tIdx) {
        return fileRepository.findAllByTypeAndIdx(fType, tIdx);
    }
}
