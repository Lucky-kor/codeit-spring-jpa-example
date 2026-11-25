package com.codeit.codeitspringjpaexample.member.direct;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaIdMappingConfig {
    private EntityManager em;
    private EntityTransaction tx;

    @Bean
    public CommandLineRunner testJpaIdMappingRunner(EntityManagerFactory factory) {
        this.em = factory.createEntityManager();
        this.tx = em.getTransaction();

        return args -> {
//            example01();
//            example02();
            example03();
        };
    }

//    private void example01() {
//        tx.begin();
//        em.persist(new Member(1L));
//        tx.commit();
//        Member member = em.find(Member.class, 1L);
//        System.out.println("# memberId: " + member.getId());
//    }

    private void example02() {
        tx.begin();
        em.persist(new Member());
        tx.commit();
        Member member = em.find(Member.class, 1L);
        System.out.println("# memberId: " + member.getId());
    }

    private void example03() {
        tx.begin();
        Member tempMember = new Member();
        tempMember.setEmail("lucky@cat.cat");
        System.out.println("# tempMemberEmail: " + tempMember.getEmail());
        em.persist(tempMember);
        Member member = em.find(Member.class, 1L);
        System.out.println("# memberId: " + member.getId());
        tx.commit();
    }
}
