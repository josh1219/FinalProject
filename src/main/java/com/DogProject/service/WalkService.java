package com.DogProject.service;

import com.DogProject.entity.WalkSession;
import com.DogProject.entity.Path;
import com.DogProject.repository.WalkSessionRepository;
import com.DogProject.repository.PathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalkService {
    
    @Autowired
    private WalkSessionRepository walkSessionRepository;
    
    @Autowired
    private PathRepository pathRepository;
    
    public List<WalkSession> getAllWalkSessions() {
        return walkSessionRepository.findAll();
    }

    public List<WalkSession> getWalkSessionsByMember(int mIdx) {
        return walkSessionRepository.findByMember_mIdxOrderByWalkDateDesc(mIdx);
    }

    public WalkSession getWalkSessionById(int wsIdx) {
        return walkSessionRepository.findById(wsIdx).orElse(null);
    }

    public List<Path> getPathsByWalkSessionOrderBySequence(int wsIdx) {
        return pathRepository.findPathsByWalkSessionOrderBySequence(wsIdx);
    }
}
