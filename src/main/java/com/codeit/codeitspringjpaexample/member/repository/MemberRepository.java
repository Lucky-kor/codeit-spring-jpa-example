// [현재 복습 대상 아님] 교안 5장 이후 Repository 활용의 비활성 수업 기록이다.
// 쿼리 메서드/JPQL/직접 SQL 예제는 확장하지 않고 보존한다. 이번에는 주석 해제하지 않는다.
// 특히 아래 과거 Member/member 이름은 현재 @Entity(name="MEMBERS") 설정과 다르다.
// 현재는 기본 find/persist와 관계의 외래 키를 이해한 뒤 README의 완료 기준까지 진행한다.
//package com.codeit.codeitspringjpaexample.member.repository;
//
//import com.codeit.codeitspringjpaexample.member.entity.Member;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MemberRepository extends JpaRepository<Member, Long> {
//    Optional<Member> findByEmail(String email);
//    Optional<Member> findByEmailAndName(String email, String name);
//
//    @Query("SELECT m FROM Member m WHERE m.name = :name") // JPQL을 직접 쿼리메서드에 할당
//    List<Member> findByNameUsingJPQL(@Param("name") String name);
//
//    @Query(value = "SELECT * FROM member WHERE email = :email", nativeQuery = true)
//    Optional<Member> findByEmailNative(@Param("email") String email);
//}
