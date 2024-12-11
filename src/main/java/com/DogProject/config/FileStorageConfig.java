package com.DogProject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class FileStorageConfig {

    @Value("${file.upload.directory}")
    private String uploadDir;

    @Bean
    CommandLineRunner initFileStorage() {
        return args -> {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                    log.info("파일 업로드 디렉토리 생성 완료: {}", uploadPath);
                } catch (Exception e) {
                    log.error("파일 업로드 디렉토리 생성 실패: {}", e.getMessage());
                    throw new RuntimeException("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
                }
            } else {
                log.info("파일 업로드 디렉토리가 이미 존재합니다: {}", uploadPath);
            }
        };
    }
}
