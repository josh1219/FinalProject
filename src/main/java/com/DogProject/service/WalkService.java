package com.DogProject.service;

import com.DogProject.entity.WalkSession;
import com.DogProject.entity.Path;
import com.DogProject.repository.WalkSessionRepository;
import com.DogProject.repository.PathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class WalkService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalkService.class);
    
    @Autowired
    private WalkSessionRepository walkSessionRepository;
    
    @Autowired
    private PathRepository pathRepository;
    
    public List<WalkSession> getAllWalkSessions() {
        return walkSessionRepository.findAll();
    }

    public List<WalkSession> getWalkSessionsByMember(int mIdx) {
        logger.info("산책 세션 조회 시작: mIdx={}", mIdx);
        List<WalkSession> sessions = walkSessionRepository.findByMember_mIdxOrderByWalkDateDesc(mIdx);
        logger.info("산책 세션 조회 결과: {} 건", sessions.size());
        for (WalkSession session : sessions) {
            logger.info("산책 세션: wsIdx={}, walkDate={}, walkEndDate={}, distance={}", 
                session.getWsIdx(), session.getWalkDate(), session.getWalkEndDate(), session.getTotalDistance());
        }
        return sessions;
    }

    public WalkSession getWalkSessionById(int wsIdx) {
        return walkSessionRepository.findById(wsIdx).orElse(null);
    }

    public List<Path> getPathsByWalkSessionOrderBySequence(int wsIdx) {
        return pathRepository.findPathsByWalkSessionOrderBySequence(wsIdx);
    }
}
