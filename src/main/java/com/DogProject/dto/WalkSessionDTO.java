package com.DogProject.dto;

import com.DogProject.entity.WalkSession;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WalkSessionDTO {
    private int wsIdx;
    private int mIdx;
    private double totalDistance;
    private LocalDateTime walkDate;
    private LocalDateTime walkEndDate;

    public static WalkSessionDTO fromEntity(WalkSession walkSession) {
        WalkSessionDTO dto = new WalkSessionDTO();
        dto.setWsIdx(walkSession.getWsIdx());
        dto.setMIdx(walkSession.getMember().getMIdx());
        dto.setTotalDistance(walkSession.getTotalDistance());
        dto.setWalkDate(walkSession.getWalkDate());
        dto.setWalkEndDate(walkSession.getWalkEndDate());
        return dto;
    }
}
