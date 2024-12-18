package com.DogProject.entity.Shopping;

import javax.persistence.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product", indexes = @Index(name = "idx_pidx", columnList = "pidx"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 고유 ID(PK)")
    @Column(name = "pidx")
    private Long pidx;          

    @Column(name = "name", nullable = false, length = 100)
    @Comment("상품 이름")
    private String name;            // 상품명

    @Column(nullable = false)
    @Comment("상품 가격")
    private int price;             // 원래 가격

    @Column(name = "stock")
    @Comment("상품 수량")
    private int stock;          // 상품 수량

    @Column(name = "imageUrl", length = 255)
    @Comment("상품 메인이미지 URL") 
    private String imageUrl;       // 상품 이미지 URL

    @Column(name = "detail_imageUrl", length = 255)
    @Comment("상품 디테일이미지 URL") 
    private String detail_imageUrl;       // 상품 이미지 URL

    @Column(name = "category", nullable = false, length = 50)
    @Comment("카테고리") 
    private String category;       // 카테고리

    @Builder.Default
    @Column(nullable = false)
    private Integer salesCount = 0;  // 판매량 (기본값 0)

    @ElementCollection
    @CollectionTable(name = "product_detail_images", joinColumns = @JoinColumn(name = "pidx"))
    @Column(name = "detail_image_url")
    @Comment("상품 상세 이미지 URL 목록")
    @Builder.Default
    private List<String> detailImageUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;       // 크롤링 일시

}
