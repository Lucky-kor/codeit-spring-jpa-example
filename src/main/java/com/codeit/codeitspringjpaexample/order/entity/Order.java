package com.codeit.codeitspringjpaexample.order.entity;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "ORDERS")
@NoArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.ORDER_REQUEST;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Getter
    public enum OrderStatus {
        ORDER_CHECK(5, "결제 확인중"),
        ORDER_REQUEST(1, "결제 완료"),
        ORDER_CONFIRM(2, "배송 준비중"),
        ORDER_COMPLETE(3, "배달 완료"),
        ORDER_CANCEL(4, "주문 취소");

        private int stepNumber;
        private String stepDescription;

        OrderStatus(int stepNumber, String stepDescription) {
            this.stepNumber = stepNumber;
            this.stepDescription = stepDescription;
        }
    }
}
