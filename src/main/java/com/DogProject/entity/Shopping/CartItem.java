package com.DogProject.entity.Shopping;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter 
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    
    private String sessionId; // 세션 ID 추가
    
    private String userId; // 사용자 ID 추가 (로그인한 경우)
}
