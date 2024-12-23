package com.DogProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogApplication.class, args);
        // test
    }

}
