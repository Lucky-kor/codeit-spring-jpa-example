package com.codeit.codeitspringjpaexample.discodedit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * [Discodeit 관계 복습] 채널 한 개를 표현한다.
 * User(사용자)와 Channel(채널)의 다대다 관계는 ReadStatus라는 중간 엔티티로 나눈다.
 * 먼저 channelId를 보고, ReadStatus.channel의 외래 키와 아래 mappedBy를 연결해 읽는다.
 * 패키지명 discodedit은 기존 수업 코드의 철자를 유지한다.
 */
@Entity // 기본 엔티티 이름은 Channel이며, 기본 테이블 매핑으로 CHANNEL을 사용한다.
@NoArgsConstructor // JPA가 조회 결과로 객체를 만들 때 사용할 기본 생성자.
@Getter
@Setter // channelName 등 일반 필드용. 컬렉션 연결은 addReadStatus를 사용한다.
public class Channel {
    @Id // Channel 객체를 식별하는 PK. ReadStatus.channel이 이 값을 참조한다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 INSERT 시 식별자를 생성한다.
    private Long channelId;

    // 채널명은 최대 50자, null 금지, 중복 금지. setter 자체에서 입력 검사를 하는 설정은 아니다.
    @Column(nullable = false, length = 50, unique = true)
    private String channelName;

    // 채널 1개 : 읽음 상태 여러 개. mappedBy에는 ReadStatus의 자바 필드명 channel을 쓴다.
    // 외래 키는 READ_STATUS.CHANNEL_ID에 있으므로 주인은 ReadStatus.channel이다.
    // 이 목록만 수정하면 DB의 CHANNEL_ID는 변경되지 않는다.
    @OneToMany(mappedBy = "channel")
    private List<ReadStatus> readStatuses = new ArrayList<>();

    /** 채널에서 읽음 상태를 연결하고 반대쪽 참조도 맞춘다. 저장/커밋은 별도로 수행한다. */
    public void addReadStatus(ReadStatus readStatus) {
        if (!readStatuses.contains(readStatus)) {
            readStatuses.add(readStatus); // 같은 객체로 두 번 호출해도 목록에는 한 번만 넣는다.
        }
        if (readStatus.getChannel() != this) {
            readStatus.setChannel(this); // 반대쪽이 이미 연결되었으면 재호출하지 않는다.
        }
    }

    // 기존 수업 코드의 호출을 유지한다. 목록 전체 교체가 아니라 한 항목 추가이므로 add가 더 명확하다.
    public void setReadStatuses(ReadStatus readStatus) {
        addReadStatus(readStatus);
    }
}
