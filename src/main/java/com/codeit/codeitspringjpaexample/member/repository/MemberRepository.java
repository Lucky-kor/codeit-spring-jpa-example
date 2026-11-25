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
