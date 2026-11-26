package com.codeit.codeitspringjpaexample.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 회원 상태 예제. 상태 1개 ↔ 회원 1명이며 FK를 가진 이 엔티티가 연관관계의 주인이다.
 * Member의 cascade=PERSIST로 새 회원과 함께 저장되고, REMOVE로 회원 삭제 시 함께 삭제된다.
 */
@Entity
@NoArgsConstructor // 조회할 때 JPA가 사용할 기본 생성자.
@Getter
@Setter
public class MemberStatus {
    @Id // 상태 자체의 기본 키. 회원의 기본 키와는 별개의 값이다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // INSERT 시 DB가 생성한다.
    private Long memberStatusId;

    @OneToOne // 참조 대상은 필드 타입 Member와 그 @Id로 결정한다.
    // MEMBER_STATUS.MEMBERS_ID → MEMBERS.MEMBER_ID. 양쪽 컬럼명이 같아야 연결되는 것은 아니다.
    @JoinColumn(name = "MEMBERS_ID", unique = true) // 이 테이블의 FK 이름. 중복 금지로 일대일 보장.
    private Member member;

    private LocalDateTime createdAt = LocalDateTime.now(); // 객체 생성 시각의 초기값.

    // 주인 필드와 Member.memberStatus를 함께 맞춘다. 이미 연결된 반대쪽에서는 재호출을 멈춘다.
    public void setMember(Member member) {
        Member previous = this.member;
        this.member = member;
        if (previous != null && previous != member && previous.getMemberStatus() == this) {
            previous.setMemberStatus(null);
        }
        if (member != null && member.getMemberStatus() != this) {
            member.setMemberStatus(this);
        }
    }
}
