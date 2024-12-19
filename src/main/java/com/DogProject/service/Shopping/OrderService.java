package com.DogProject.service.Shopping;

import com.DogProject.entity.Shopping.Order;
import com.DogProject.repository.Shopping.OrderRepository;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;

    public List<Order> getOrderHistory(String userEmail, String period, String status) {
        LocalDateTime startDate = calculateStartDate(period);
        int mIdx = memberService.findBymEmail(userEmail).getMIdx();
        
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByMember_mIdxAndOrderDateAfterAndStatus(mIdx, startDate, status);
        }
        return orderRepository.findByMember_mIdxAndOrderDateAfter(mIdx, startDate);
    }

    public List<Order> getReturnHistory(String userEmail, String period) {
        LocalDateTime startDate = calculateStartDate(period);
        int mIdx = memberService.findBymEmail(userEmail).getMIdx();
        return orderRepository.findByMember_mIdxAndOrderDateAfterAndStatusIn(
            mIdx, 
            startDate, 
            List.of("RETURNED", "RETURN_REQUESTED")
        );
    }

    private LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        if (period == null || period.isEmpty()) {
            return now.minusMonths(3); // 기본값: 3개월
        }
        
        switch (period) {
            case "1w":
                return now.minusWeeks(1);
            case "1m":
                return now.minusMonths(1);
            case "3m":
                return now.minusMonths(3);
            case "6m":
                return now.minusMonths(6);
            default:
                return now.minusMonths(3);
        }
    }
}
