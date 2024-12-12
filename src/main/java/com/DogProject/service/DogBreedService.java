package com.DogProject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;

import java.util.*;

@Slf4j
@Service
public class DogBreedService {
    
    @Value("${claude.api.key}")
    private String apiKey;
    
    private final WebClient webClient;
    
    public DogBreedService(@Value("${claude.api.key}") String apiKey) {
        log.info("Initializing DogBreedService with API key length: {}", apiKey.length());
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }
    
    public String identifyBreed(MultipartFile image) throws Exception {
        log.info("Starting breed identification for image: {}", image.getOriginalFilename());
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        log.debug("Converted image to base64, length: {}", base64Image.length());
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-opus-20240229");
        requestBody.put("max_tokens", 100);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        List<Map<String, Object>> content = new ArrayList<>();
        
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "이미지에 있는 강아지의 품종이 무엇인지 알려주세요. 품종 이름만 간단히 한글로 답변해주세요.");
        content.add(textContent);
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image");
        Map<String, Object> source = new HashMap<>();
        source.put("type", "base64");
        source.put("media_type", "image/jpeg");
        source.put("data", base64Image);
        imageContent.put("source", source);
        content.add(imageContent);
        
        message.put("content", content);
        messages.add(message);
        requestBody.put("messages", messages);
        
        try {
            log.info("Sending request to Claude API");
            JsonNode response = webClient.post()
                    .uri("/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            log.debug("Received response from Claude API: {}", response);
            
            if (response != null && response.has("content") && 
                response.get("content").isArray() && 
                response.get("content").size() > 0) {
                String breed = response.get("content").get(0).get("text").asText().trim();
                log.info("Successfully identified breed: {}", breed);
                return breed;
            } else {
                log.error("Invalid API response format: {}", response);
                throw new RuntimeException("견종을 식별할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("Error calling Claude API: ", e);
            throw new RuntimeException("견종 식별 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
