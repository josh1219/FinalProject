package com.DogProject.entity.Shopping;

import javax.persistence.*;

import com.DogProject.entity.Member;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 Idx")
    private int oIdx; // 주문 ID (Primary Key)

    @ManyToOne
    @Comment("주문 회원 Idx")
    @JoinColumn(name = "mIdx", nullable = false) // Member 테이블과 연관
    private Member member; // 주문한 회원

    @Column(nullable = false)
    @Comment("총 주문 금액")
    private int oTotalAmount; // 총 주문 금액

    @Column(nullable = false, updatable = false)
    @Comment("주문 시간")
    private LocalDateTime oCreatedTime = LocalDateTime.now(); // 주문 생성 시간

    @Column(length = 255)
    @Comment("베송지")
    private String oAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("주문 상태")
    private OrderStatus oStatus = OrderStatus.PENDING; // 주문 상태

    @Column(length = 255)
    @Comment("운송장 번호")
    private String oWaybillNumber;

    @Column(length = 255)
    @Comment("받는 사람")
    private String oRecipient;

    @Column(length = 255)
    @Comment("받는 사람 전화번호")
    private String oRecipientPhone;

    @Column(length = 255)
    @Comment("배송 요청 사항")
    private String oRequest;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails; // 주문 상세 정보 (1:N 관계)

    // 주문 상태 열거형
    public enum OrderStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
