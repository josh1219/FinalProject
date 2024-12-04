package com.DogProject.entity.Shopping;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pIdx; // 상품 ID (Primary Key)

    @Column(nullable = false)
    private String pName; // 상품 이름

    @Column(nullable = false)
    private int pPrice; // 원래 가격 (정수형)

    @Column(nullable = false)
    private int PDiscount; // 할인율 (정수형)

    @Column(nullable = false)
    private int pStock; // 재고

    @Column(columnDefinition = "TEXT")
    private String pDescription; // 상품 설명


    @Column(nullable = false)
    private String pCategory; // 상품 카테고리

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime pCreateTime = java.time.LocalDateTime.now(); // 생성 시간

    @Column(nullable = false)
    private java.time.LocalDateTime pUpdatedTime = java.time.LocalDateTime.now(); // 수정 시간

    @PreUpdate
    public void onPreUpdate() {
        this.pUpdatedTime = java.time.LocalDateTime.now();
    }
}

