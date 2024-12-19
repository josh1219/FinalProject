package com.DogProject.repository.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findBySessionId(String sessionId);
    List<CartItem> findByUserId(String userId);
    
    Optional<CartItem> findByProductAndSessionId(Product product, String sessionId);
    Optional<CartItem> findByProductAndUserId(Product product, String userId);
    
    void deleteByProductAndSessionId(Product product, String sessionId);
    void deleteByProductAndUserId(Product product, String userId);
}
