package com.DogProject.repository.Shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DogProject.entity.Shopping.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContaining(String keyword);

    // 신상품순 정렬
    List<Product> findAllByOrderByCreatedAtDesc();
    
    // 인기순 정렬
    List<Product> findAllByOrderBySalesCountDesc();
    
    // 가격 낮은순 정렬
    List<Product> findAllByOrderByPriceAsc();
    
    // 가격 높은순 정렬
    List<Product> findAllByOrderByPriceDesc();
}
