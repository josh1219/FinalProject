package com.DogProject.entity.Shopping;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.DogProject.entity.Member;

@Entity
@Table(name = "cart_items")
@Getter 
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_idx")
    private Long cIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_idx")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "p_idx")
    private Product product;

    @Column(nullable = false)
    private int quantity;
}
