package com.codeit.codeitspringjpaexample.order.entity;

import com.codeit.codeitspringjpaexample.coffee.entity.Coffee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [연관관계 복습: 다대다 양방향 매핑] 주문 한 건에 포함된 커피 한 종류와 수량을 담는 중간 엔티티.
 * 주문 N : 커피 M을 Order 1 : N OrderCoffee N : 1 Coffee로 풀어낸다.
 * 단순 연결만 하는 @ManyToMany 대신 기존 예제의 이 구조로 수량까지 저장한다.
 * Order.orderCoffees와 Coffee.orderCoffees를 통해 양쪽에서 주문 항목을 탐색할 수 있다.
 */
@Entity
@NoArgsConstructor // JPA가 조회 결과로 객체를 만들 때 필요한 기본 생성자.
@Getter
@Setter // setOrder/setCoffee는 반대쪽 목록을 갱신하지 않는다. 아래 편의 메서드를 사용한다.
public class OrderCoffee {
    @Id // 연결된 주문/커피의 ID와 별도로 주문 항목 자체를 식별하는 기본 키.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 시 ID를 생성한다.
    private Long orderCoffeeId;

    @ManyToOne // 여러 주문 항목이 주문 하나에 속한다.
    @JoinColumn(name = "ORDER_ID") // 주문 항목 테이블의 FK. 이 필드가 주문 연결의 주인이다.
    private Order order;

    @ManyToOne // 여러 주문 항목이 같은 커피 종류를 참조할 수 있다.
    @JoinColumn(name = "COFFEE_ID") // 주문 항목 테이블의 FK. 이 필드가 커피 연결의 주인이다.
    private Coffee coffee;

    // 주문별 수량은 주문과 커피의 관계에 속한 정보이다. 예제에서 2잔/1잔으로 직접 설정한다.
    // nullable=false는 null 제약이며, 양수 여부를 검사하는 설정은 아니다.
    @Column(nullable = false)
    private int quantity;

    // 주문 항목에서 주문을 연결하고 주문의 목록도 맞춘다. 관계 변경 시 이전 목록에서도 제거한다.
    public void addOrder(Order order) {
        if (this.order != null && this.order != order) {
            this.order.getOrderCoffees().remove(this);
        }
        this.order = order;
        if (order != null && !order.getOrderCoffees().contains(this)) {
            order.addOrderCoffee(this);
        }
    }

    // Coffee 쪽에서도 주문 항목을 탐색할 수 있도록 두 객체의 참조를 함께 설정한다.
    public void addCoffee(Coffee coffee) {
        if (this.coffee != null && this.coffee != coffee) {
            this.coffee.getOrderCoffees().remove(this);
        }
        this.coffee = coffee;
        if (coffee != null && !coffee.getOrderCoffees().contains(this)) {
            coffee.addOrderCoffee(this);
        }
    }
}
