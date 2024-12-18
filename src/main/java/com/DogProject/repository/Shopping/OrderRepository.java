package com.DogProject.repository.Shopping;

import com.DogProject.entity.Shopping.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderDateAfter(LocalDateTime startDate);
    List<Order> findByOrderDateAfterAndStatus(LocalDateTime startDate, String status);
    List<Order> findByOrderDateAfterAndStatusIn(LocalDateTime startDate, List<String> statuses);
}
