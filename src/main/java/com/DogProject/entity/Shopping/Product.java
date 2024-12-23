package com.DogProject.entity.Shopping;

import javax.persistence.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "p_idx")
    @Comment("상품 고유 ID(PK)")
    private int pIdx;          

    @Column(length = 100, nullable = false)
    @Comment("상품 이름")
    private String name;            // 상품명

    @Column(nullable = false)
    @Comment("상품 가격")
    private int price;             // 원래 가격

    @Column
    @Comment("상품 수량")
    private int stock;          // 상품 수량

    @Column
    @Comment("상품 이미지 URL")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(
        name = "product_detail_images",
        joinColumns = @JoinColumn(name = "p_idx")
    )
    @Column(name = "detail_image_url")
    @Comment("상품 상세 이미지 URL 목록")
    private List<String> detailImageUrls = new ArrayList<>();

    @Column(length = 50, nullable = false)
    @Comment("카테고리") 
    private String category;       // 카테고리

    @Column(nullable = false)
    private int salesCount;  // 판매량 

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;       // 크롤링 일시

}
