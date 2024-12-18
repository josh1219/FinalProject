package com.DogProject.service.Shopping;

import com.DogProject.entity.Shopping.Order;
import com.DogProject.repository.Shopping.OrderRepository;
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

    public List<Order> getOrderHistory(String period, String status) {
        LocalDateTime startDate = calculateStartDate(period);
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByOrderDateAfterAndStatus(startDate, status);
        }
        return orderRepository.findByOrderDateAfter(startDate);
    }

    public List<Order> getReturnHistory(String period) {
        LocalDateTime startDate = calculateStartDate(period);
        return orderRepository.findByOrderDateAfterAndStatusIn(
            startDate, 
            List.of("RETURNED", "RETURN_REQUESTED")
        );
    }

    private LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        if (period == null) {
            return now.minusMonths(1); // 기본값: 1개월
        }

        switch (period) {
            case "today":
                return now.toLocalDate().atStartOfDay();
            case "1month":
                return now.minusMonths(1);
            case "3months":
                return now.minusMonths(3);
            case "6months":
                return now.minusMonths(6);
            default:
                return now.minusMonths(1); // 기본값: 1개월
        }
    }
}
