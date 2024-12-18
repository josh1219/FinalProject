package com.DogProject.entity.Shopping;

import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

@Entity
@Table(name = "order_items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 상품 고유 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @Comment("주문 수량")
    private int quantity;

    @Column(nullable = false)
    @Comment("주문 당시 가격")
    private int price;
}
