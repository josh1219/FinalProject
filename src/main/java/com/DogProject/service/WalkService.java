package com.DogProject.service;

import com.DogProject.entity.WalkSession;
import com.DogProject.repository.WalkSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalkService {
    
    @Autowired
    private WalkSessionRepository walkSessionRepository;
    
    public List<WalkSession> getAllWalkSessions() {
        return walkSessionRepository.findAll();
    }
}
