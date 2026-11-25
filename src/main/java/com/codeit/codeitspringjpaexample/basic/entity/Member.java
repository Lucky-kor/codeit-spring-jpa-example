package com.codeit.codeitspringjpaexample.basic.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue
    private Long memberId;
    private String email;

    public Member(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "id: " + memberId + ", email: " + email;
    }
}
