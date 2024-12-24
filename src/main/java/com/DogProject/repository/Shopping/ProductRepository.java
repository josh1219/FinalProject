package com.DogProject.repository.Shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DogProject.entity.Shopping.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // 전체 상품 정렬
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findAllByOrderBySalesCountDesc(); // 인기순
    List<Product> findAllByOrderByPriceAsc();       // 낮은가격순
    List<Product> findAllByOrderByPriceDesc();      // 높은가격순

    // 카테고리별 정렬
    List<Product> findByCategoryOrderBySalesCountDesc(String category); // 카테고리 + 인기순
    List<Product> findByCategoryOrderByPriceAsc(String category);       // 카테고리 + 낮은가격순
    List<Product> findByCategoryOrderByPriceDesc(String category);      // 카테고리 + 높은가격순
}
