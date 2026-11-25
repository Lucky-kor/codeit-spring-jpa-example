package com.codeit.codeitspringjpaexample.coffee.entity;

import com.codeit.codeitspringjpaexample.order.entity.OrderCoffee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [연관관계 복습: 다대다 관계] 판매하는 커피 한 종류를 표현한다.
 * 주문과 커피의 다대다 관계를 OrderCoffee라는 주문 항목 엔티티로 나누어 표현한다.
 * OrderCoffee.coffee를 먼저 읽고 아래 orderCoffees를 읽으면 주인과 반대쪽을 비교할 수 있다.
 */
@Entity // 기본 엔티티 이름은 Coffee. 객체의 필드와 DB 테이블의 컬럼을 매핑한다.
@NoArgsConstructor // JPA가 조회 시 사용할 기본 생성자.
@Getter
@Setter // 관계 컬렉션의 setter는 반대쪽을 동기화하지 않는다. 연결에는 addOrderCoffee를 사용한다.
public class Coffee {
    @Id // 커피 한 종류를 구분하는 기본 키.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB의 INSERT 결과로 식별자를 받는다.
    private Long coffeeId;

    // 한글 이름과 영문 이름은 각각 null을 허용하지 않는 최대 50자 컬럼이다.
    @Column(nullable = false, length = 50)
    private String korName;
    @Column(nullable = false, length = 50)
    private String engName;

    // 한 종류의 단가. 주문 수량은 이 엔티티가 아니라 OrderCoffee에 저장한다.
    @Column(nullable = false)
    private int price;

    // ARC, CLT처럼 3자 이내의 코드. length는 문자열 컬럼의 길이를 정한다.
    @Column(nullable = false, length = 3)
    private String coffeeCode;

    // 두 시간 모두 객체 생성 시 초기화된다. 저장/수정 시각을 자동 관리하는 기능은 사용하지 않는다.
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    // 커피 1종 : 주문 항목 여러 개. coffee는 OrderCoffee 안의 자바 필드명이다.
    // COFFEE_ID 외래 키를 쓰는 주인은 OrderCoffee.coffee이며, 이 목록은 반대쪽 탐색용이다.
    @OneToMany(mappedBy = "coffee")
    private List<OrderCoffee> orderCoffees = new ArrayList<>();

    // 커피 정보를 준비하는 생성자. new만으로 DB에 저장되지는 않는다.
    public Coffee(String korName, String engName, int price, String coffeeCode) {
        this.korName = korName;
        this.engName = engName;
        this.price = price;
        this.coffeeCode = coffeeCode;
    }

    // 커피에서 주문 항목을 연결한다. persist와 커밋은 실행 예제에서 명시적으로 수행한다.
    public void addOrderCoffee(OrderCoffee orderCoffee) {
        if (!orderCoffees.contains(orderCoffee)) {
            orderCoffees.add(orderCoffee);
        }
        // 이미 반대쪽까지 연결되었다면 호출을 끝내 중복 호출을 막는다.
        if (orderCoffee.getCoffee() != this) {
            orderCoffee.addCoffee(this);
        }
    }
}
