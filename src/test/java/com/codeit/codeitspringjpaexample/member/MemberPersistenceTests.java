package com.codeit.codeitspringjpaexample.member;

import com.codeit.codeitspringjpaexample.coffee.entity.Coffee;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.entity.MemberStatus;
import com.codeit.codeitspringjpaexample.member.service.MemberService;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import com.codeit.codeitspringjpaexample.order.entity.OrderCoffee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * cascade 저장/삭제, 고아 객체 제거와 N+1 개선을 실제 DB 결과로 확인한다.
 * SQL 통계는 쿼리 수를 비교하기 위한 테스트 보조 도구이며 API 구현에는 사용하지 않는다.
 */
@SpringBootTest(properties = {"study.lesson=none", "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
@ActiveProfiles("local")
class MemberPersistenceTests {
    @Autowired
    private EntityManagerFactory factory;
    @Autowired
    private MemberService service;

    private EntityManager em;
    private EntityTransaction tx;

    @BeforeEach
    void open() {
        em = factory.createEntityManager();
        tx = em.getTransaction();
    }

    @AfterEach
    void close() {
        if (tx.isActive()) {
            tx.rollback();
        }
        em.close();
    }

    /** Member만 persist해도 연결한 새 MemberStatus가 저장되는지 확인한다. */
    @Test
    void persistCascadesToMemberStatus() {
        tx.begin();
        Member member = new Member("persist@cat.cat", "저장 전파", "010-8100-0001");
        MemberStatus status = new MemberStatus();
        member.setMemberStatus(status);
        em.persist(member);
        tx.commit();
        assertNotNull(status.getMemberStatusId());

        em.clear();
        tx.begin();
        MemberStatus reloaded = em.find(MemberStatus.class, status.getMemberStatusId());
        assertEquals(member.getMemberId(), reloaded.getMember().getMemberId());
        assertEquals(status.getMemberStatusId(),
                em.find(Member.class, member.getMemberId()).getMemberStatus().getMemberStatusId());
        tx.commit();
    }

    /** 회원 → 주문 → 항목과 회원 → 상태는 삭제되지만 여러 주문이 공유하는 Coffee는 남는다. */
    @Test
    void removeCascadesToOwnedDataAndKeepsCoffee() {
        tx.begin();
        Member member = new Member("remove@cat.cat", "삭제 전파", "010-8100-0002");
        MemberStatus status = new MemberStatus();
        member.setMemberStatus(status);
        em.persist(member);
        Coffee coffee = new Coffee("아메리카노", "Americano", 3500, "ARC");
        em.persist(coffee);
        Order order = new Order();
        member.addOrder(order);
        em.persist(order);
        OrderCoffee item = new OrderCoffee();
        item.addOrder(order);
        item.addCoffee(coffee);
        item.setQuantity(2);
        em.persist(item);
        tx.commit();

        em.clear();
        tx.begin();
        em.remove(em.find(Member.class, member.getMemberId()));
        tx.commit();
        em.clear();
        tx.begin();
        assertNull(em.find(Member.class, member.getMemberId()));
        assertNull(em.find(MemberStatus.class, status.getMemberStatusId()));
        assertNull(em.find(Order.class, order.getOrderId()));
        assertNull(em.find(OrderCoffee.class, item.getOrderCoffeeId()));
        assertNotNull(em.find(Coffee.class, coffee.getCoffeeId()));
        tx.commit();
    }

    /** 회원은 유지한 채 주문 목록에서 한 항목을 빼면, 해당 주문 행만 고아 객체로 삭제된다. */
    @Test
    void removingOrderFromCollectionDeletesOrphan() {
        tx.begin();
        Member member = new Member("orphan@cat.cat", "고아 제거", "010-8100-0003");
        em.persist(member);
        Order removed = new Order();
        Order kept = new Order();
        member.addOrder(removed);
        member.addOrder(kept);
        em.persist(removed);
        em.persist(kept);
        tx.commit();

        em.clear();
        tx.begin();
        Member managed = em.find(Member.class, member.getMemberId());
        managed.removeOrder(em.find(Order.class, removed.getOrderId()));
        tx.commit();
        em.clear();
        tx.begin();
        assertNotNull(em.find(Member.class, member.getMemberId()));
        assertNull(em.find(Order.class, removed.getOrderId()));
        assertNotNull(em.find(Order.class, kept.getOrderId()));
        assertEquals(1, em.find(Member.class, member.getMemberId()).getOrders().size());
        tx.commit();
    }

    /** 세 조회 방식이 같은 회원을 반환하면서 주문 로딩에 필요한 SQL 수는 줄어드는지 비교한다. */
    @Test
    void fetchJoinAndEntityGraphKeepSameMembersWithFewerQueries() {
        tx.begin();
        for (int i = 0; i < 3; i++) {
            Member member = new Member("nplus" + i + "@cat.cat", "N+1 " + i,
                    "010-8200-000" + i);
            member.setMemberStatus(new MemberStatus());
            em.persist(member);
            if (i < 2) {
                Order order = new Order();
                member.addOrder(order);
                em.persist(order);
            }
        }
        tx.commit();
        em.clear();

        Statistics statistics = factory.unwrap(SessionFactory.class).getStatistics();
        // 서비스의 각 호출이 별도 트랜잭션/컨텍스트에서 orders를 접근한다.
        // 같은 컨텍스트의 1차 캐시가 N+1 비교 결과를 가리지 않게 한다.
        statistics.clear();
        List<Long> basic = queryAndReadOrders("basic");
        long basicCount = statistics.getPrepareStatementCount();
        statistics.clear();
        List<Long> fetched = queryAndReadOrders("fetch");
        long fetchCount = statistics.getPrepareStatementCount();
        statistics.clear();
        List<Long> graph = queryAndReadOrders("graph");
        long graphCount = statistics.getPrepareStatementCount();

        assertEquals(basic, fetched); // 주문 없는 회원도 빠지지 않아야 한다.
        assertEquals(basic, graph);
        assertTrue(basicCount > fetchCount, "일반 조회가 fetch join보다 많은 SQL을 사용해야 한다.");
        assertTrue(basicCount > graphCount, "일반 조회가 EntityGraph보다 많은 SQL을 사용해야 한다.");
    }

    // 테스트 안에 조회문을 다시 만들지 않고 API와 같은 실제 서비스/Repository 코드를 검증한다.
    private List<Long> queryAndReadOrders(String mode) {
        return service.findTestMembers(mode).stream().map(Member::getMemberId).sorted().toList();
    }
}
