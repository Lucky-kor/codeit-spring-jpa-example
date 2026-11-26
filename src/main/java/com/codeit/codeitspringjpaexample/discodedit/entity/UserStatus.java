package com.codeit.codeitspringjpaexample.discodedit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [Discodeit 일대일 복습] 사용자 한 명에 대응하는 상태 엔티티.
 * 자체 PK userStatusId와 사용자 참조용 FK user_id는 별개 컬럼이다.
 * ReadStatus.user에도 user_id라는 FK가 있지만 각자 자기 테이블에 있는 독립된 컬럼이다.
 */
@Entity // 기본 엔티티 이름 UserStatus, 기본 네이밍에 따른 테이블 이름 USER_STATUS.
@NoArgsConstructor // JPA 조회 시 사용할 기본 생성자.
@Setter
@Getter
public class UserStatus {
    @Id // 상태 행 자체의 기본 키. 이 값과 연결된 사용자의 userId가 같을 필요는 없다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userStatusId;

    @OneToOne // 외래 키를 가진 이 필드가 주인이고 User.userStatus가 반대쪽이다.
    // name은 현재 USER_STATUS 테이블의 컬럼명이다. 대상은 필드 타입 User의 @Id이다.
    // USER_STATUS.USER_ID → USERS.USER_ID. unique=true로 한 사용자에 상태 한 개만 연결한다.
    // null 연결은 허용하므로 관계 해제 시 행은 남고 USER_ID만 null이 된다.
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /** 주인에서 사용자를 바꾼다. 이전 사용자와 새 사용자의 userStatus도 함께 정리한다. */
    public void setUser(User user) {
        if (this.user == user) {
            return;
        }
        User previous = this.user;
        this.user = user;

        if (previous != null && previous.getUserStatus() == this) {
            previous.setUserStatus(null);
        }
        if (user != null && user.getUserStatus() != this) {
            user.setUserStatus(this);
        }
    }
}
