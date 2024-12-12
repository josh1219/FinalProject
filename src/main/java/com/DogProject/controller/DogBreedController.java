package com.DogProject.controller;

import com.DogProject.service.DogBreedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class DogBreedController {
    
    private final DogBreedService dogBreedService;
    private static final Logger log = LoggerFactory.getLogger(DogBreedController.class);
    
    @Autowired
    public DogBreedController(DogBreedService dogBreedService) {
        this.dogBreedService = dogBreedService;
    }
    
    @PostMapping("/identify-breed")
    public ResponseEntity<String> identifyBreed(@RequestParam("image") MultipartFile image) {
        try {
            log.info("Received image for breed identification: {}", image.getOriginalFilename());
            String breed = dogBreedService.identifyBreed(image);
            log.info("Successfully identified breed: {}", breed);
            return ResponseEntity.ok(breed);
        } catch (Exception e) {
            log.error("Error in breed identification: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
