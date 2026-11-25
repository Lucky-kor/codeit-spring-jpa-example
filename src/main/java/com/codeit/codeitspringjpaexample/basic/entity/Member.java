package com.codeit.codeitspringjpaexample.basic.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [ORM과 영속성 컨텍스트 복습] 가장 작은 회원 엔티티.
 * 먼저 이 클래스를 읽고 basic/JpaBasicConfig의 example01부터 순서대로 실행한다.
 * member/entity/Member와 클래스 이름은 같지만 패키지와 엔티티 이름이 다른 별도 예제이다.
 */
@Entity // JPA가 관리하는 클래스. 기본 엔티티 이름은 클래스 이름인 Member이다.
@NoArgsConstructor // JPA가 조회 결과로 객체를 만들 때 사용할 기본 생성자를 Lombok이 생성한다.
@Getter // 아래 필드를 읽는 getter를 생성한다. DB 매핑을 지정하는 애너테이션은 아니다.
@Setter // 영속 상태인 객체의 값을 바꾼 뒤 커밋하여 변경 감지를 관찰한다.
public class Member {
    @Id // 엔티티를 구별할 식별자이며 테이블의 기본 키(PK)에 대응한다.
    @GeneratedValue // 전략을 생략하면 AUTO. 실제 생성 방법은 JPA 구현체와 DB에 따라 결정된다.
    private Long memberId;

    // 매핑을 생략한 일반 필드도 기본적으로 컬럼에 매핑된다.
    private String email;

    // new Member(...)만으로는 DB에 저장되지 않는다. persist를 호출하기 전에는 비영속 상태다.
    public Member(String email) {
        this.email = email;
    }

    // 예제 출력에서 식별자와 이메일을 읽기 쉽게 표시한다. 이 메서드는 SQL을 만들지 않는다.
    @Override
    public String toString() {
        return "id: " + memberId + ", email: " + email;
    }
}
