package com.DogProject.entity.Shopping;

import javax.persistence.*;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.DogProject.entity.Member;

@Entity
@Table(name = "cart_items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("장바구니 아이템 고유 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @Comment("상품 수량")
    private int quantity;

    // 장바구니에 상품을 추가하는 메서드
    public static CartItem createCartItem(Member member, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setMember(member);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
