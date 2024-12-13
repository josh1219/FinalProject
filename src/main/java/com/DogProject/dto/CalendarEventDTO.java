package com.DogProject.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CalendarEventDTO {
    private String id;
    private String title;
    private String content;
    private LocalDateTime start;
    private LocalDateTime end;
    private String type; // "schedule" 또는 "walk"
    private Double totalDistance; // 산책인 경우 총 거리
}
