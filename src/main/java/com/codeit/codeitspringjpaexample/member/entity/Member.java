package com.codeit.codeitspringjpaexample.member.entity;

import com.codeit.codeitspringjpaexample.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor // JPA가 엔티티를 생성할 수 있도록 기본 생성자를 둔다.
@Getter
@Setter // 관계 필드의 setter는 한쪽 값만 바꾼다. 양쪽 연결에는 아래 addOrder를 사용한다.
@Entity(name = "MEMBERS") // 엔티티 이름 지정. @Table은 DB 테이블 이름을 별도로 지정할 때 사용한다.
public class Member {
    @Id // MEMBERS의 기본 키이자 영속성 컨텍스트에서 회원을 구분하는 값이다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 시 식별자를 생성한다.
    private Long memberId;
    @Column(nullable = false, unique = true, updatable = false)
    private String email;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 13, unique = true)
    private String phone;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();
    @Transient // 객체에서만 사용하는 값으로, DB 컬럼을 만들거나 저장하지 않는다.
    private String age;
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
    // nullable=false 컬럼에 필요한 값을 준비한다. 식별자는 persist 시 DB에서 생성한다.
    public Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }
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
