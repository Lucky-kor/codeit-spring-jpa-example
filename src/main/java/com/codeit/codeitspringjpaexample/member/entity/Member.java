package com.codeit.codeitspringjpaexample.member.entity;

import com.codeit.codeitspringjpaexample.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "MEMBERS")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Transient
    private String age;

    private List<Order> orders = new ArrayList<>();

    public Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }
}
