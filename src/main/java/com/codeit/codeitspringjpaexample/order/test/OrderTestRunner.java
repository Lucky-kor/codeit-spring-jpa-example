package com.codeit.codeitspringjpaexample.order.test;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderTestRunner {
    private EntityManager em;
    private EntityTransaction tx;

    @Bean
    public CommandLineRunner testJpaBasicRunner(EntityManagerFactory emFactory) {
        this.em = emFactory.createEntityManager();
        this.tx = em.getTransaction();

        return args -> {
//            enumTestCase();
            mappingManyToOneTest();
        };
    }

    private void enumTestCase() {
        tx.begin();
        Order order = new Order();
        em.persist(order);
        tx.commit();
    }

    private void mappingManyToOneTest() {
        tx.begin();
        Member member = new Member("lucky@cat.cat", "luckyKim", "010-1234-1234");
        em.persist(member);

        Order order = new Order();
        order.setMember(member);
        em.persist(order);

        tx.commit();

        Order findOrder = em.find(Order.class, 1L);
        System.out.println(findOrder.getMember().getName());

        Member findMember = em.find(Member.class, 1L);
        findMember.
    }
}
