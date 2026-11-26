package com.codeit.codeitspringjpaexample.discodedit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * [Discodeit 관계 복습] 사용자 1명은 여러 채널별 ReadStatus와 최대 1개의 UserStatus를 가진다.
 * 읽는 순서: User/Channel의 PK → ReadStatus의 두 주인 필드 → UserStatus의 일대일 주인 필드.
 * Member 학습 예제와 별도 엔티티·테이블을 사용하므로 기존 MEMBERS 초기 데이터와 섞이지 않는다.
 */
@Entity(name = "users") // 엔티티 이름. @Table이 없어 기본 테이블 이름도 users를 사용한다.
@NoArgsConstructor // JPA가 사용할 기본 생성자. 예제에서는 생성 후 setUsername으로 이름을 채운다.
@Getter
@Setter // userStatus는 아래 직접 구현한 setter가 양쪽 참조를 함께 맞춘다.
public class User {
    @Id // USERS 테이블의 PK. ReadStatus와 UserStatus가 각각 이 사용자를 참조한다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID를 직접 넣지 않고 DB에서 생성한다.
    private Long userId;

    // null을 허용하지 않는 최대 50자 이름. @Column은 DB 매핑 설정이다.
    @Column(nullable = false, length = 50)
    private String username;

    // 사용자 1명 : 채널별 읽음 상태 여러 개. user는 ReadStatus의 자바 필드명이다.
    // USERS 테이블에 ReadStatus용 FK가 추가되는 것이 아니다. FK는 READ_STATUS.USER_ID에 있다.
    @OneToMany(mappedBy = "user")
    private List<ReadStatus> readStatuses = new ArrayList<>();

    // 사용자 ↔ 사용자 상태 일대일 관계의 반대쪽. 주인은 UserStatus.user이다.
    // mappedBy="user"는 위 관계와 글자가 같지만 이번에는 UserStatus 안의 user 필드를 가리킨다.
    // 서로 다른 대상 엔티티의 필드이므로 두 관계는 독립적이다.
    @OneToOne(mappedBy = "user")
    private UserStatus userStatus;

    /** 상태를 연결/교체/해제한다. 양쪽 객체만 맞추며 persist/remove를 자동 실행하지 않는다. */
    public void setUserStatus(UserStatus userStatus) {
        if (this.userStatus == userStatus) {
            return; // 같은 연결의 반복 호출은 여기서 멈춘다.
        }
        UserStatus previous = this.userStatus;
        this.userStatus = userStatus;

        // 먼저 내 참조를 바꿨으므로 반대쪽에서 다시 호출하더라도 무한 반복하지 않는다.
        if (previous != null && previous.getUser() == this) {
            previous.setUser(null);
        }
        if (userStatus != null && userStatus.getUser() != this) {
            userStatus.setUser(this);
        }
    }

    /** 사용자에서 읽음 상태를 연결한다. FK를 쓰는 ReadStatus.user까지 함께 설정한다. */
    public void addReadStatus(ReadStatus readStatus) {
        if (!readStatuses.contains(readStatus)) {
            readStatuses.add(readStatus);
        }
        if (readStatus.getUser() != this) {
            readStatus.setUser(this);
        }
    }

    // 기존 수업 코드의 메서드를 보존한다. 여러 항목의 목록을 통째로 바꾸는 setter는 아니다.
    public void setReadStatuses(ReadStatus readStatus) {
        addReadStatus(readStatus);
    }
}
