package com.DogProject.repository.Shopping;

import com.DogProject.entity.Shopping.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMember_mIdxAndOrderDateAfter(int mIdx, LocalDateTime startDate);
    List<Order> findByMember_mIdxAndOrderDateAfterAndStatus(int mIdx, LocalDateTime startDate, String status);
    List<Order> findByMember_mIdxAndOrderDateAfterAndStatusIn(int mIdx, LocalDateTime startDate, List<String> statuses);
}
