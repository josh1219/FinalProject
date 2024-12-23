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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<InputStreamResource> serveFile(@PathVariable String fileName) throws IOException {
        try {
            // URL 디코딩 및 파일명 정리
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            // | 문자로 분리하여 첫 번째 부분(실제 파일명)만 사용
            String actualFileName = decodedFileName.split("\\|")[0];
            
            // 파일 경로 생성
            Path filePath = Paths.get("src/main/resources/static/uploads", actualFileName);
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

            // 비디오 파일인 경우 특별 처리
            if (contentType.startsWith("video/")) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + actualFileName)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(new InputStreamResource(new FileInputStream(file)));
            }

            // 일반 파일 처리
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(file.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + actualFileName)
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
