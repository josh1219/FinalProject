package com.DogProject.entity.Shopping;

import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = @Index(name = "idx_order_id", columnList = "id"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 고유 ID")
    private Long id;

    @Column(nullable = false)
    @Comment("주문 날짜")
    private LocalDateTime orderDate;

    @Column(nullable = false)
    @Comment("주문 상태")
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    @Comment("총 주문 금액")
    private int totalAmount;

    @PrePersist
    public void prePersist() {
        this.orderDate = LocalDateTime.now();
    }
}
