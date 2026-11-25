package com.codeit.codeitspringjpaexample.coffee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Coffee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coffeeId;
    @Column(nullable = false, length = 50)
    private String korName;
    @Column(nullable = false, length = 50)
    private String engName;
    @Column(nullable = false)
    private int price;
    @Column(nullable = false, length = 3)
    private String coffeeCode;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    public Coffee(String korName, String engName, int price, String coffeeCode) {
        this.korName = korName;
        this.engName = engName;
        this.price = price;
        this.coffeeCode = coffeeCode;
    }
}
