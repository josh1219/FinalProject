package com.DogProject.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.io.Resource;

@Controller
@RequestMapping("/tour")
public class TourController {
    
    private static final Logger logger = LoggerFactory.getLogger(TourController.class);
    
    @GetMapping("/map")
    public String showTourMap(String place) {
        return "tour/tourMain";
    }
    
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getTourData() {
        try {
            logger.info("Loading DogTour.json file...");
            Resource resource = new ClassPathResource("static/DogTour.json");
            if (!resource.exists()) {
                logger.error("DogTour.json file not found!");
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resource.getFile().toPath()), java.nio.charset.StandardCharsets.UTF_8);
            logger.info("DogTour.json file loaded successfully");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(content);
        } catch (IOException e) {
            logger.error("Error loading tour data: ", e);
            return ResponseEntity.internalServerError().body("Error loading tour data: " + e.getMessage());
        }
    }
}
