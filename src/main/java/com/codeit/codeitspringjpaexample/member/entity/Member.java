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
 * 회원의 컬럼 설계 → 주문 양방향 매핑 → 회원 상태 일대일 → cascade 순서로 읽는다.
 * Repository 페이징에서도 이 엔티티를 사용한다. basic/entity/Member와 import를 구별한다.
 */
@NoArgsConstructor // JPA가 엔티티를 생성할 수 있도록 기본 생성자를 둔다.
@Getter
@Setter // 생성되는 setOrders는 한쪽만 바꾼다. 주문은 addOrder, 상태는 직접 구현한 setMemberStatus로 연결한다.
@Entity(name = "MEMBERS") // 엔티티 이름 지정. @Table은 DB 테이블 이름을 별도로 지정할 때 사용한다.
public class Member {
    @Id // MEMBERS의 기본 키이자 영속성 컨텍스트에서 회원을 구분하는 값이다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 시 식별자를 생성한다.
    private Long memberId;
    // 필수/중복 금지. updatable=false는 객체 setter를 막는 것이 아니라 UPDATE SQL에서 제외한다.
    @Column(nullable = false, unique = true, updatable = false)
    private String email;
    @Column(nullable = false, length = 100) // 이름은 필수이며 문자열 컬럼은 최대 100자다.
    private String name;
    @Column(nullable = false, length = 13, unique = true) // 예: 010-1234-1234
    private String phone;
    @Column(nullable = false) // 객체 생성 시 초기값이다. DB나 JPA가 자동 관리하는 시간이 아니다.
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) // 회원을 수정해도 자동 변경되지 않으므로 필요하면 직접 수정한다.
    private LocalDateTime modifiedAt = LocalDateTime.now();
    @Transient // 객체에서만 사용하는 값으로, DB 컬럼을 만들거나 저장하지 않는다.
    private String age;
    // mappedBy는 Order의 자바 필드명이다. MEMBER_ID 외래 키의 주인은 Order.member다.
    // 기본 LAZY: 회원 목록만 조회한 뒤 각 orders를 접근하면 추가 SELECT가 생겨 N+1을 관찰할 수 있다.
    // REMOVE: 회원 자체를 삭제하면 그 회원의 주문도 삭제한다. 새 주문의 저장(PERSIST)은 전파하지 않는다.
    // orphanRemoval: 관리 중인 회원의 목록에서 주문을 제거하면 flush/commit 시 그 주문 행을 삭제한다.
    // 따라서 이미 저장된 주문을 다른 회원에게 옮기는 용도로 이 목록을 사용하지 않는다.
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    // 외래 키는 MemberStatus.member에 있다. 양방향 객체 연결과 저장 전파는 서로 다른 기능이다.
    // PERSIST: 새 회원 persist/save 때 연결한 새 상태도 INSERT한다.
    // REMOVE: 회원 DELETE 때 상태도 지워 회원을 참조하는 외래 키가 남지 않게 한다.
    // 기본 EAGER인 일대일도 부가 조회를 만들 수 있어 N+1 비교의 fetch join/graph에 함께 포함한다.
    @OneToOne(mappedBy = "member", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private MemberStatus memberStatus;


    // nullable=false 컬럼에 필요한 값을 준비한다. 식별자는 persist 시 DB에서 생성한다.
    public Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }
    // 양쪽 메모리 참조를 맞춘다. 이 메서드는 persist/commit을 실행하지 않는다.
    public void addOrder(Order order) {
        if (!orders.contains(order)) {
            orders.add(order);
        }
        // 반대쪽이 이미 연결되어 있으면 다시 호출하지 않아 무한 재귀를 막는다.
        if (order.getMember() != this) {
            order.addMember(this);
        }
    }

    // 이미 조회되어 관리 중인 회원에서 호출하면, 목록에서 빠진 주문은 고아 객체 삭제 대상이다.
    public void removeOrder(Order order) {
        orders.remove(order);
        if (order.getMember() == this) {
            order.addMember(null);
        }
    }

    // 이전 상태와의 연결도 정리한다. null은 객체 연결 해제이며, 상태의 자동 DELETE 설정은 아니다.
    public void setMemberStatus(MemberStatus memberStatus) {
        MemberStatus previous = this.memberStatus;
        this.memberStatus = memberStatus;
        if (previous != null && previous != memberStatus && previous.getMember() == this) {
            previous.setMember(null);
        }
        if (memberStatus != null && memberStatus.getMember() != this) {
            memberStatus.setMember(this);
        }
    }
}
