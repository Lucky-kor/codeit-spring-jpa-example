package com.codeit.codeitspringjpaexample.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기존 HTTP 예제에서 요청값을 담던 일반 자바 객체다. @Entity가 없어 DB 테이블과 매핑되지 않는다.
 * Controller/Service/Repository는 현재 진도 이후의 비활성 코드이므로 복습 실행에는 사용하지 않는다.
 */
@NoArgsConstructor // Lombok이 기본 생성자를 만든다. 아래 Getter/Setter도 메서드 생성용이다.
@Getter
@Setter
public class MemberPostDto {
    private String name; // 요청에서 받은 이름. 이 필드 선언만으로 DB에 저장되는 것은 아니다.
    private String email; // 요청에서 받은 이메일. 엔티티의 email 필드와 역할을 구분한다.
}
