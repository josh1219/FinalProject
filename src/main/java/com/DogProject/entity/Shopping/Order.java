package com.DogProject.entity.Shopping;

import com.DogProject.entity.Member;
import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "o_idx")
    @Comment("주문 고유 ID")
    private Long oIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_idx")
    @Comment("주문자")
    private Member member;

    @Column(nullable = false)
    @Comment("주문 날짜")
    private LocalDateTime orderDate;

    @Column(nullable = false)
    @Comment("사용한 포인트")
    private int usedPoint;

    @Column(nullable = false)
    @Comment("주문 상태")
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    @Comment("총 주문 금액")
    private int totalAmount;

    @Column(nullable = false)
    @Comment("수령인 이름")
    private String recipientName;

    @Column(nullable = false)
    @Comment("수령인 전화번호")
    private String recipientPhone;

    @Column(nullable = false)
    @Comment("배송지 주소")
    private String recipientAddress;

    @Column
    @Comment("배송 요청사항")
    private String shippingRequest;

    @PrePersist
    public void prePersist() {
        this.orderDate = LocalDateTime.now();
    }
}
