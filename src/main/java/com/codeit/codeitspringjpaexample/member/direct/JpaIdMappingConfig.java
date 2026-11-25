package com.codeit.codeitspringjpaexample.member.direct;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 복습 2: member/entity/Member의 식별자와 컬럼 설계를 실제 DB 결과와 비교한다.
 * 실행: --study.lesson=mapping --study.example=identity
 * 과거 실습의 getId(), 필수값 없는 new Member(), 중복 commit을 현재 엔티티에 맞게 보완했다.
 */
@Configuration
@ConditionalOnProperty(name = "study.lesson", havingValue = "mapping")
public class JpaIdMappingConfig {
    private EntityManager em;
    private EntityTransaction tx;

    @Bean
    public CommandLineRunner jpaIdMappingRunner(EntityManagerFactory factory,
                                               @Value("${study.example:identity}") String example) {
        return args -> {
            em = factory.createEntityManager();
            tx = em.getTransaction();
            try {
                System.out.println("[mapping] 선택한 예제: " + example);
                switch (example) {
                    case "identity" -> identity();
                    case "column" -> column();
                    case "transient" -> transientField();
                    case "updatable" -> updatable();
                    case "unique" -> constraintViolation(true);
                    case "nullable" -> constraintViolation(false);
                    default -> throw new IllegalArgumentException(
                            "mapping 예제: identity, column, transient, updatable, unique, nullable");
                }
            } finally {
                if (tx.isActive()) {
                    tx.rollback(); // 예상 밖 오류가 나도 열린 트랜잭션을 취소한다.
                }
                em.close();
            }
        };
    }

    /** IDENTITY: ID를 직접 넣지 않고 DB가 생성하게 한다. */
    private void identity() {
        tx.begin();
        Member member = new Member("identity@cat.cat", "식별자 실습", "010-1111-1111");
        System.out.println("[identity] persist 전 ID: " + member.getMemberId()); // null
        em.persist(member); // ID 확보를 위해 여기서 INSERT가 나갈 수 있다.
        System.out.println("[identity] persist 후 ID: " + member.getMemberId());
        tx.commit(); // INSERT 실행 시점과 트랜잭션 확정 시점을 구별한다.
        em.clear();
        tx.begin();
        Member found = em.find(Member.class, member.getMemberId());
        System.out.println("[identity] DB 재조회 이름: " + found.getName());
        tx.commit();
    }

    /** @Column: 필수값을 저장하고 일반 필드의 변경 감지를 확인한다. */
    private void column() {
        tx.begin();
        Member member = new Member("column@cat.cat", "변경 전", "010-2222-2222");
        em.persist(member);
        tx.commit();
        em.clear();
        tx.begin();
        Member found = em.find(Member.class, member.getMemberId());
        found.setName("변경 후"); // name은 updatable=false가 없어 UPDATE 대상이다.
        found.setModifiedAt(found.getModifiedAt().plusSeconds(1)); // 날짜도 직접 변경해야 한다.
        tx.commit();
        em.clear();
        tx.begin();
        System.out.println("[column] DB 이름: " + em.find(Member.class, member.getMemberId()).getName());
        tx.commit();
    }

    /** @Transient: 객체에는 값이 있지만 테이블 컬럼에는 저장되지 않는다. */
    private void transientField() {
        tx.begin();
        Member member = new Member("transient@cat.cat", "임시 필드 실습", "010-3333-3333");
        member.setAge("20");
        em.persist(member);
        tx.commit();
        System.out.println("[transient] 저장 직후 객체의 age: " + member.getAge()); // 20
        em.clear(); // 같은 객체를 다시 보는 착각을 피한다.
        tx.begin();
        Member found = em.find(Member.class, member.getMemberId());
        System.out.println("[transient] DB 재조회 객체의 age: " + found.getAge()); // null
        tx.commit();
    }

    /** updatable=false는 setter를 막는 것이 아니라 UPDATE SQL에서 제외한다. */
    private void updatable() {
        tx.begin();
        Member member = new Member("original@cat.cat", "이메일 실습", "010-4444-4444");
        em.persist(member);
        tx.commit();
        em.clear();
        tx.begin();
        Member found = em.find(Member.class, member.getMemberId());
        found.setEmail("changed@cat.cat"); // 메모리에서는 바뀐다.
        found.setName("이름은 수정됨"); // UPDATE 로그에서 email이 빠지는지 비교한다.
        tx.commit();
        System.out.println("[updatable] 메모리 이메일: " + found.getEmail()); // changed@cat.cat
        em.clear();
        tx.begin();
        Member reloaded = em.find(Member.class, member.getMemberId());
        System.out.println("[updatable] DB 이메일: " + reloaded.getEmail()); // original@cat.cat
        System.out.println("[updatable] DB 이름: " + reloaded.getName()); // 이름은 수정됨
        tx.commit();
    }

    /** unique/nullable: 다른 필수값은 정상으로 넣고 제약 하나의 실패를 관찰한다. */
    private void constraintViolation(boolean duplicateEmail) {
        String label = duplicateEmail ? "unique" : "nullable";
        tx.begin();
        try {
            if (duplicateEmail) {
                em.persist(new Member("same@cat.cat", "회원 1", "010-5555-5555"));
                em.persist(new Member("same@cat.cat", "회원 2", "010-6666-6666"));
            } else {
                em.persist(new Member("null@cat.cat", null, "010-7777-7777"));
            }
            // IDENTITY는 persist 때 오류가 날 수도 있어 그 시점부터 예외를 처리한다.
            em.flush();
        } catch (PersistenceException expected) {
            System.out.println("[" + label + "] 예상한 제약 위반: " + expected.getClass().getSimpleName());
            System.out.println("[" + label + "] 위쪽 로그의 email 중복 또는 name null 원인을 확인하세요.");
            return;
        } finally {
            // 실패 실습의 데이터는 성공/실패 모두 취소한다.
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        throw new IllegalStateException("예상한 " + label + " 제약 위반이 발생하지 않았습니다.");
    }
}
