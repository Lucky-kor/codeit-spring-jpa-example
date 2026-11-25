package com.codeit.codeitspringjpaexample.order.entity;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [연관관계 복습] 주문은 회원 한 명을 참조하고 여러 주문 항목을 가진다.
 * 읽는 순서: member 필드(다대일의 주인) → Member.orders(일대다의 반대쪽)
 * → orderCoffees → OrderCoffee.order. 실행은 order/test/OrderTestRunner에서 확인한다.
 */
@Entity(name = "ORDERS") // JPA 엔티티 이름. SQL 예약어 ORDER와 구분되는 이름을 사용한다.
@NoArgsConstructor // JPA가 사용할 기본 생성자.
@Getter
@Setter // 관계 setter는 한쪽만 바꾼다. 양방향 연결에는 addMember/addOrderCoffee를 사용한다.
public class Order {
    @Id // 주문의 기본 키. 이 값을 주문 항목의 ORDER_ID가 참조한다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 때 식별자를 생성한다.
    private Long orderId;

    @Enumerated(EnumType.STRING) // enum 상수 이름(ORDER_REQUEST)을 문자열로 저장한다.
    private OrderStatus orderStatus = OrderStatus.ORDER_REQUEST;

    // 객체 생성 시각의 초기값. 별도 자동 갱신 처리를 추가하지 않았으므로 변경 시각도 자동 변경되지 않는다.
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @ManyToOne // 여러 주문이 한 회원을 참조한다. 주문 → 회원 방향으로 객체를 탐색한다.
    @JoinColumn(name = "MEMBER_ID") // ORDERS 테이블의 외래 키 컬럼. Order.member가 관계의 주인이다.
    private Member member;

    // 주문 1개 : 주문 항목 여러 개. mappedBy의 order는 OrderCoffee 안의 필드명이다.
    // 외래 키 ORDER_ID는 반대쪽 OrderCoffee.order가 관리한다.
    @OneToMany(mappedBy = "order")
    private List<OrderCoffee> orderCoffees = new ArrayList<>();

    // 주문에서 시작하여 항목의 order도 연결한다. 같은 항목을 두 번 넣지 않는다.
    public void addOrderCoffee(OrderCoffee orderCoffee) {
        if (!orderCoffees.contains(orderCoffee)) {
            orderCoffees.add(orderCoffee);
        }
        // 반대쪽에서 다시 호출되어도 여기서 멈추므로 무한 재귀가 발생하지 않는다.
        if (orderCoffee.getOrder() != this) {
            orderCoffee.addOrder(this);
        }
    }

    // 숫자/설명은 자바 enum이 가진 부가 정보이다. DB에는 상수 이름만 저장된다.
    // EnumType.ORDINAL은 상수의 선언 순서에 의존하므로 여기서는 STRING으로 명시한다.
    @Getter
    public enum OrderStatus {
        ORDER_CHECK(5, "결제 확인중"),
        ORDER_REQUEST(1, "결제 완료"),
        ORDER_CONFIRM(2, "배송 준비중"),
        ORDER_COMPLETE(3, "배달 완료"),
        ORDER_CANCEL(4, "주문 취소");

        private int stepNumber; // 화면 등에 표시할 진행 단계 번호. DB 저장값이 아니다.
        private String stepDescription; // 상태 설명. DB 저장값이 아니다.

        // 각 상수의 번호와 설명을 초기화한다.
        OrderStatus(int stepNumber, String stepDescription) {
            this.stepNumber = stepNumber;
            this.stepDescription = stepDescription;
        }
    }

    // 관계의 주인인 주문에서 회원을 연결한다. 인자로 null을 주면 객체의 연결을 해제한다.
    // 회원이 바뀌는 경우 이전 회원의 목록에서도 제거해 객체 양쪽의 상태를 맞춘다.
    public void addMember(Member member) {
        if (this.member != null && this.member != member) {
            this.member.getOrders().remove(this);
        }
        this.member = member; // 이 필드가 DB의 MEMBER_ID에 반영될 값이다.
        if (member != null && !member.getOrders().contains(this)) {
            member.addOrder(this);
        }
    }
}
