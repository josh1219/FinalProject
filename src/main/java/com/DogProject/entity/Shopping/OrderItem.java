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
    @Column(name = "oi_idx")
    @Comment("주문 상품 고유 ID")
    private Long oiIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "o_idx")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_idx")
    private Product product;

    @Column(nullable = false)
    @Comment("주문 수량")
    private int quantity;

    @Column(nullable = false)
    @Comment("주문 당시 가격")
    private int price;
}
