package com.codeit.codeitspringjpaexample.member.repository;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * [API 실습 3] JpaRepository가 기본 저장/ID 조회/삭제를 제공한다. 엔티티와 ID 타입을 함께 선언한다.
 * 아래에서 메서드명으로 만드는 파생 쿼리 → JPQL → 네이티브 SQL → 연관관계 조회 순으로 비교한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    // findBy + 필드명: 이메일/전화번호 조건을 만들어 조회한다. 없을 때는 빈 Optional이다.
    Optional<Member> findByEmail(String email);
    Optional<Member> findByPhone(String phone);

    // JPQL은 테이블/컬럼 대신 엔티티 이름(@Entity(name="MEMBERS"))과 자바 필드명을 사용한다.
    @Query("SELECT m FROM MEMBERS m WHERE m.name = :username")
    List<Member> findQueryByUsername(@Param("username") String username);

    // nativeQuery는 실제 DB 테이블/컬럼을 사용한다. :username은 문자열 연결이 아닌 파라미터 바인딩이다.
    @Query(value = "SELECT * FROM members WHERE name = :username", nativeQuery = true)
    List<Member> findNativeQueryByUsername(@Param("username") String username);

    // Slice 반환 파생 쿼리: size + 1개를 조회해 다음 묶음 여부를 알아내며 전체 count는 하지 않는다.
    // Slice 실습은 page 번호에 따른 offset을, 첫 cursor 요청은 offset=0을 사용한다.
    Slice<Member> findAllByOrderByMemberIdDesc(Pageable pageable);

    // cursor 이후는 ID < cursor인 회원이다. ID와 커서를 모두 Long으로 맞춰 큰 식별자도 처리한다.
    Slice<Member> findAllByMemberIdLessThanOrderByMemberIdDesc(Long cursor, Pageable pageable);

    // LEFT JOIN이라 주문/상태가 없는 회원도 남는다. fetch로 연관 데이터까지 한 번에 읽는다.
    // orders는 컬렉션이므로 한 회원이 여러 행으로 나올 수 있어 distinct로 회원의 중복을 제거한다.
    // 역방향 일대일 memberStatus도 함께 가져와 이 관계의 EAGER 조회가 SQL 비교에 끼어들지 않게 한다.
    @Query("SELECT DISTINCT m FROM MEMBERS m LEFT JOIN FETCH m.orders LEFT JOIN FETCH m.memberStatus ORDER BY m.memberId DESC")
    List<Member> findAllWithOrders();

    // EntityGraph는 JPQL에 fetch join을 직접 쓰는 대신 함께 읽을 관계를 선언한다.
    @EntityGraph(attributePaths = {"orders", "memberStatus"})
    @Query("SELECT DISTINCT m FROM MEMBERS m ORDER BY m.memberId DESC")
    List<Member> findAllWithOrdersEntityGraph();
}
