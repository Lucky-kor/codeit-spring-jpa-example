package com.codeit.codeitspringjpaexample.discodedit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [Discodeit 관계 복습] 어느 사용자가 어느 채널에 연결되는지 표현하는 중간 엔티티.
 * User 1 : N ReadStatus N : 1 Channel 구조이며, 양쪽 부모에 역방향 목록이 있다.
 * 이 예제는 외래 키 연결에 집중하므로 읽은 시각 등 새로운 필드는 추가하지 않는다.
 */
@Entity // 기본 엔티티 이름 ReadStatus는 기본 네이밍에 따라 READ_STATUS 테이블에 매핑된다.
@NoArgsConstructor // JPA 조회용 기본 생성자.
@Getter
@Setter // 아래 관계 setter는 직접 구현하여 양쪽 객체의 참조를 함께 맞춘다.
public class ReadStatus {
    @Id // 사용자/채널의 ID와 별개로 연결 한 건 자체를 식별하는 PK.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 저장 시 ID를 생성한다.
    private Long readStatusId;

    @ManyToOne // 여러 ReadStatus가 같은 사용자 한 명을 참조할 수 있다.
    // name은 '현재 엔티티'인 READ_STATUS 테이블에 만들 FK 컬럼명이다.
    // 참조 대상은 문자열 user_id로 고르는 것이 아니라 필드 타입 User와 그 @Id(userId)로 결정된다.
    // 즉 READ_STATUS.USER_ID → USERS.USER_ID이며 ReadStatus.user가 관계의 주인이다.
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne // 여러 ReadStatus가 채널 하나를 참조한다.
    // 현재 테이블의 READ_STATUS.CHANNEL_ID → 필드 타입 Channel의 @Id인 CHANNEL.CHANNEL_ID.
    // 다른 엔티티에서도 name="channel_id"를 쓰더라도 서로 다른 테이블의 컬럼이다.
    @JoinColumn(name = "channel_id")
    private Channel channel;

    /** 주인 필드 user와 User.readStatuses를 동기화한다. null은 연결 해제이며 행 삭제가 아니다. */
    public void setUser(User user) {
        if (this.user != null && this.user != user) {
            this.user.getReadStatuses().remove(this); // 사용자 변경 시 이전 사용자의 목록에서도 제거한다.
        }
        this.user = user;
        if (user != null && !user.getReadStatuses().contains(this)) {
            user.addReadStatus(this); // contains 검사로 두 편의 메서드의 무한 재호출을 막는다.
        }
    }

    /** 주인 필드 channel과 Channel.readStatuses를 맞춘다. 커밋 시 바뀐 FK가 DB에 반영된다. */
    public void setChannel(Channel channel) {
        if (this.channel != null && this.channel != channel) {
            this.channel.getReadStatuses().remove(this);
        }
        this.channel = channel;
        if (channel != null && !channel.getReadStatuses().contains(this)) {
            channel.addReadStatus(this);
        }
    }
}
