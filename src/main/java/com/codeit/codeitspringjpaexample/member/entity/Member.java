package com.codeit.codeitspringjpaexample.member.entity;

import com.codeit.codeitspringjpaexample.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [엔티티 설계와 연관관계 복습] 실제 회원 컬럼과 회원의 주문 목록을 표현한다.
 * 기본 키/컬럼은 member/direct/JpaIdMappingConfig, 주문 관계는 order/test/OrderTestRunner에서 확인한다.
 * basic/entity/Member와 다른 엔티티이므로 import할 때 패키지를 반드시 확인한다.
 */
@NoArgsConstructor // JPA가 엔티티를 생성할 수 있도록 기본 생성자를 둔다.
@Getter
@Setter // 관계 필드의 setter는 한쪽 값만 바꾼다. 양쪽 연결에는 아래 addOrder를 사용한다.
@Entity(name = "MEMBERS") // 엔티티 이름 지정. @Table은 DB 테이블 이름을 별도로 지정할 때 사용한다.
public class Member {
    @Id // MEMBERS의 기본 키이자 영속성 컨텍스트에서 회원을 구분하는 값이다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 시 식별자를 생성한다.
    private Long memberId;

    // null 금지, 중복 금지, UPDATE SQL에서 제외. 객체에서 setEmail을 막는 설정은 아니다.
    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    // 문자열 컬럼의 최대 길이와 null 허용 여부를 지정한다.
    @Column(nullable = false, length = 100)
    private String name;

    // 예제 전화번호(010-1234-1234)는 13자. DB 컬럼에 중복을 허용하지 않는다.
    @Column(nullable = false, length = 13, unique = true)
    private String phone;

    // 객체를 생성한 시각의 초기값이다. DB가 자동으로 넣어 주는 값은 아니다.
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 현재 예제에서는 초기값만 설정한다. 회원을 수정해도 이 시간이 자동 갱신되지는 않는다.
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @Transient // 객체에서만 사용하는 값으로, DB 컬럼을 만들거나 저장하지 않는다.
    private String age;

    // 회원 1명 : 주문 여러 개. mappedBy에는 컬럼명이 아니라 Order의 필드명 "member"를 적는다.
    // 이 컬렉션은 반대쪽 탐색용이다. 외래 키 MEMBER_ID를 쓰는 주인은 Order.member이다.
    // 따라서 orders에만 추가하고 Order.member를 비워 두면 DB의 회원 연결은 저장되지 않는다.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    // nullable=false 컬럼에 필요한 값을 준비한다. 식별자는 persist 시 DB에서 생성한다.
    public Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    /**
     * 회원에서 출발해 양쪽 객체를 함께 연결한다. 이 메서드 자체가 DB 저장을 수행하지는 않는다.
     * 같은 주문을 다시 연결해도 목록에 중복으로 담지 않는다.
     */
    public void addOrder(Order order) {
        if (!orders.contains(order)) {
            orders.add(order);
        }
        // 반대쪽이 이미 연결되어 있으면 다시 호출하지 않아 무한 재귀를 막는다.
        if (order.getMember() != this) {
            order.addMember(this);
        }
    }
}
