package com.codeit.codeitspringjpaexample.basic;

import com.codeit.codeitspringjpaexample.basic.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class JpaBasicConfig {
    private EntityManager em;
    private EntityTransaction tx;

    @Bean
    public CommandLineRunner testJpaBasicRunner(EntityManagerFactory emFactory) {
        this.em = emFactory.createEntityManager();
        this.tx = em.getTransaction();

        return args -> {
            // 스프링이 실행되면 이 부분의 코드가 실행될거에요
//            example01();
//            example02();
//            example03();
//            example04();
            example05();
        };
    }

    private void example01() {
        Member member = new Member("lucky@cat.house");

        // persist로 영속화
        em.persist(member);

        Member resultMember = em.find(Member.class, 1L);
        System.out.println(resultMember);
    }

    private void example02() {
        tx.begin();
        Member member = new Member("lucky@cat.cat");

        // 영속화
        em.persist(member);
        // 커밋 -> 영속성 컨텍스트에 변경사항을 DB로 전파
        tx.commit();

        Member resultMember1 = em.find(Member.class, 1L);
        System.out.println(resultMember1);

        Member resultMember2 = em.find(Member.class, 2L);
        System.out.println(resultMember2 == null);
    }

    private void example03() {
        tx.begin();

        Member member1 = new Member("lucky@cat.cat");
        Member member2 = new Member("latte@cat.cat");

        em.persist(member1);
        em.persist(member2);

        tx.commit();
    }

    private void example04() {
        tx.begin();
        em.persist(new Member("lucky@cat.cat"));
        tx.commit();

        tx.begin();
        Member findMember = em.find(Member.class, 1L);
        findMember.setEmail("vanilla@cat.cat");
        tx.commit();
    }

    private void example05() {
        tx.begin();
        em.persist(new Member("lucky@cat.cat"));
        tx.commit();

        tx.begin();
        Member findMember = em.find(Member.class, 1L);
        em.remove(findMember);
        tx.commit();
    }
}
