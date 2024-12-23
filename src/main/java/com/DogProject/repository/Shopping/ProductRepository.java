package com.DogProject.repository.Shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DogProject.entity.Shopping.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // 카테고리별 정렬
    List<Product> findByCategoryOrderByCreatedAtDesc(String category);
    List<Product> findByCategoryOrderBySalesCountDesc(String category);
    List<Product> findByCategoryOrderByPriceAsc(String category);
    List<Product> findByCategoryOrderByPriceDesc(String category);

    // 전체 상품 정렬
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findAllByOrderBySalesCountDesc();
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
}
