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
import java.util.UUID;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload.directory}")
    private String uploadDir;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource(uploadDir);
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
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Resource resource = resourceLoader.getResource(uploadDir);
        Path uploadPath = Paths.get(resource.getFile().getAbsolutePath());
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        file.setFileSaveName(uniqueFilename);
        file.setFileRealName(originalFilename);

        return fileRepository.save(file);
    }

    @Transactional
    public void deleteFile(File file) {
        if (file != null) {
            try {
                Resource resource = resourceLoader.getResource(uploadDir);
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

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    public File getFileByTIdx(int dIdx) {
        return fileRepository.findByTIdx(dIdx)
                .orElseThrow(() -> new RuntimeException("File not found with tIdx: " + dIdx));
    }
}
