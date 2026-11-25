package com.codeit.codeitspringjpaexample.basic;

import com.codeit.codeitspringjpaexample.basic.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 복습 1: ORM과 영속성 컨텍스트. basic/entity/Member를 먼저 읽고 01~06 순서로 실행한다.
 * 실행: --study.lesson=basic --study.example=01 (README의 전체 명령 참고).
 * 직접 EntityManager를 사용하므로 HTTP 요청이나 Repository가 필요 없다.
 */
@Configuration
@ConditionalOnProperty(name = "study.lesson", havingValue = "basic")
public class JpaBasicConfig {
    // Factory는 스프링이 관리한다. 직접 만든 EntityManager만 예제 실행 후 닫는다.
    private EntityManager em;
    private EntityTransaction tx;

    @Bean
    public CommandLineRunner jpaBasicRunner(EntityManagerFactory factory,
                                           @Value("${study.example:01}") String example) {
        // CommandLineRunner는 애플리케이션 초기화가 끝나면 한 번 실행된다.
        return args -> {
            em = factory.createEntityManager();
            tx = em.getTransaction();
            try {
                System.out.println("[basic] 선택한 예제: " + example);
                switch (example) {
                    case "01" -> example01();
                    case "02" -> example02();
                    case "03" -> example03();
                    case "04" -> example04();
                    case "05" -> example05();
                    case "06" -> example06();
                    default -> throw new IllegalArgumentException("basic 예제: 01, 02, 03, 04, 05, 06");
                }
            } finally {
                // 오류가 났다면 남은 트랜잭션을 취소하고, 직접 만든 자원을 정리한다.
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        };
    }

    /** 01. 비영속 객체를 영속화하고 같은 컨텍스트에서 동일 객체를 찾는다. */
    private void example01() {
        tx.begin(); // INSERT/UPDATE/DELETE는 활성 트랜잭션 안에서 수행한다.
        Member member = new Member("lucky@cat.house"); // new만 하면 비영속 객체다.
        System.out.println("[01] persist 전 관리 여부: " + em.contains(member)); // false
        em.persist(member); // 영속성 컨텍스트가 member를 관리하기 시작한다.
        Member found = em.find(Member.class, member.getMemberId()); // 생성된 ID를 사용한다.
        System.out.println("[01] persist 후 관리 여부: " + em.contains(member)); // true
        System.out.println("[01] 같은 객체인가: " + (member == found)); // true: 1차 캐시
        tx.commit(); // flush 후 트랜잭션 확정. persist 자체가 commit은 아니다.
    }

    /** 02. 1차 캐시를 비운 뒤 DB 조회와 이후의 캐시 조회를 비교한다. */
    private void example02() {
        tx.begin();
        Member member = new Member("lucky@cat.cat");
        em.persist(member);
        tx.commit();
        Long id = member.getMemberId();

        em.clear(); // 관리 중인 객체를 분리한다. DB 행을 지우는 명령이 아니다.
        tx.begin();
        Member first = em.find(Member.class, id); // 캐시가 비었으므로 DB SELECT 확인.
        Member second = em.find(Member.class, id); // 같은 컨텍스트의 캐시에서 반환.
        System.out.println("[02] 재조회 결과: " + first);
        System.out.println("[02] 두 번의 find가 같은 객체인가: " + (first == second)); // true
        System.out.println("[02] 기존 객체와 재조회 객체가 같은가: " + (member == first)); // false
        System.out.println("[02] 저장하지 않은 음수 ID는 null인가: " + (em.find(Member.class, -1L) == null));
        tx.commit();
    }

    /** 03. 쓰기 지연과 flush를 SQL 로그로 관찰한다. flush는 commit과 다르다. */
    private void example03() {
        tx.begin();
        em.persist(new Member("lucky@cat.cat"));
        em.persist(new Member("latte@cat.cat"));
        // 이 Member는 AUTO 전략이다. ID 확보용 SQL은 먼저 나올 수 있다.
        // IDENTITY를 쓰는 다른 엔티티에서는 ID 확보를 위해 INSERT가 persist 때 나갈 수도 있다.
        System.out.println("[03] flush 호출 전: 앞뒤 SQL 로그의 INSERT 위치를 비교하세요.");
        em.flush(); // DB에 동기화하지만 트랜잭션은 확정하지 않는다.
        System.out.println("[03] flush 후 트랜잭션이 진행 중인가: " + tx.isActive()); // true
        tx.commit(); // flush로 보낸 변경까지 최종 확정한다.
    }

    /** 04. 관리 중인 엔티티의 필드 변경을 감지하여 UPDATE를 만든다. */
    private void example04() {
        tx.begin();
        Member member = new Member("lucky@cat.cat");
        em.persist(member);
        tx.commit();
        Long id = member.getMemberId();
        em.clear();

        tx.begin();
        Member found = em.find(Member.class, id);
        found.setEmail("vanilla@cat.cat"); // persist나 별도 update 호출은 필요 없다.
        tx.commit(); // 조회 당시 상태와 비교하여 변경을 반영한다.
        em.clear(); // 메모리만 바뀐 것이 아닌지 DB를 다시 조회한다.
        tx.begin();
        System.out.println("[04] DB에 반영된 이메일: " + em.find(Member.class, id).getEmail());
        tx.commit();
    }

    /** 05. remove는 삭제를 예약하고 commit 뒤 조회 결과는 null이다. */
    private void example05() {
        tx.begin();
        Member member = new Member("lucky@cat.cat");
        em.persist(member);
        tx.commit();
        Long id = member.getMemberId();
        em.clear();

        tx.begin();
        Member found = em.find(Member.class, id);
        em.remove(found); // 관리 중인 객체를 삭제 대상으로 지정한다.
        tx.commit();
        em.clear();
        tx.begin();
        System.out.println("[05] 삭제 후 조회 결과가 null인가: " + (em.find(Member.class, id) == null));
        tx.commit();
    }

    /** 06. 준영속: clear 후 객체를 수정해도 변경 감지가 적용되지 않는다. */
    private void example06() {
        tx.begin();
        Member member = new Member("original@cat.cat");
        em.persist(member);
        tx.commit();
        Long id = member.getMemberId();

        tx.begin();
        em.clear(); // member가 컨텍스트에서 분리된다(준영속).
        member.setEmail("detached@cat.cat"); // 자바 객체 값만 바뀐다.
        System.out.println("[06] clear 후 관리 여부: " + em.contains(member)); // false
        tx.commit();

        tx.begin();
        System.out.println("[06] 분리된 객체 이메일: " + member.getEmail()); // detached@cat.cat
        System.out.println("[06] DB 이메일: " + em.find(Member.class, id).getEmail()); // original@cat.cat
        tx.commit();
    }
}
