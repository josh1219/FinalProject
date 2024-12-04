package com.DogProject.entity.Shopping;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 상세 Idx")
    private int oDIdx; // 주문 상세 ID (Primary Key)

    @ManyToOne
    @JoinColumn(name = "oIdx", nullable = false) // Orders 테이블과 연관
    private Orders order; // 주문 정보

    @ManyToOne
    @JoinColumn(name = "pIdx", nullable = false) // Products 테이블과 연관
    private Product product; // 상품 정보

    @Column(nullable = false)
    @Comment("주문 수량")
    private int quantity; // 주문 수량

    @Column(nullable = false)
    @Comment("상품 수량 총 금액")
    private int price; // 상품 수량 총 금액
}

