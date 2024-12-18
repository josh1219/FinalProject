package com.DogProject.repository.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByProduct(Product product);
}
