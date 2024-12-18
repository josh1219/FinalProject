package com.DogProject.controller;

import com.DogProject.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<InputStreamResource> serveFile(@PathVariable String fileName) throws IOException {
        // 파일 경로 생성
        Path filePath = Paths.get("src/main/resources/static/uploads", fileName);
        File file = filePath.toFile();
        
        // 파일이 존재하지 않으면 404 반환
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 파일의 MIME 타입 확인
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 파일 스트림 생성
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamResource resource = new InputStreamResource(fileInputStream);

        // ResponseEntity 생성
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
