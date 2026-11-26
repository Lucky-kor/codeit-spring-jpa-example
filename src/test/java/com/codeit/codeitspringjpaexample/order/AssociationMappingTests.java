package com.codeit.codeitspringjpaexample.order;

import com.codeit.codeitspringjpaexample.coffee.entity.Coffee;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import com.codeit.codeitspringjpaexample.order.entity.OrderCoffee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실습을 보강하는 자동 검증이다. 학습 순서는 README와 Runner를 먼저 따라가면 된다.
 * H2만 사용하고 시작 실습은 끈다. 모든 검증은 저장 → commit → clear → 재조회 결과를 확인한다.
 */
@SpringBootTest(properties = "study.lesson=none")
@ActiveProfiles("local")
class AssociationMappingTests {
    @Autowired
    private EntityManagerFactory emFactory;

    private EntityManager em;
    private EntityTransaction tx;

    // 각 검증에서 직접 사용할 영속성 컨텍스트를 열고, 종료할 때 반드시 정리한다.
    @BeforeEach
    void openEntityManager() {
        em = emFactory.createEntityManager();
        tx = em.getTransaction();
    }

    @AfterEach
    void closeEntityManager() {
        if (tx.isActive()) {
            tx.rollback();
        }
        em.close();
    }

    /** 같은 커피가 여러 주문에 포함되는 다대다 관계와 주문별 수량을 실제 DB에서 확인한다. */
    @Test
    void joinEntityPersistsBothDirectionsAndQuantities() {
        tx.begin();
        Member member = new Member("join@cat.cat", "주문회원", "010-1111-1111");
        Coffee americano = new Coffee("아메리카노", "Americano", 3500, "ARC");
        Coffee latte = new Coffee("카페 라테", "Caffe Latte", 4500, "CLT");
        em.persist(member);
        em.persist(americano);
        em.persist(latte);

        Order firstOrder = new Order();
        Order secondOrder = new Order();
        firstOrder.addMember(member);
        secondOrder.addMember(member);
        em.persist(firstOrder);
        em.persist(secondOrder);

        OrderCoffee firstAmericano = new OrderCoffee();
        firstAmericano.addOrder(firstOrder);
        firstAmericano.addCoffee(americano);
        firstAmericano.setQuantity(2);
        OrderCoffee firstLatte = new OrderCoffee();
        firstLatte.addOrder(firstOrder);
        firstLatte.addCoffee(latte);
        firstLatte.setQuantity(1);
        OrderCoffee secondAmericano = new OrderCoffee();
        secondAmericano.addOrder(secondOrder);
        secondAmericano.addCoffee(americano);
        secondAmericano.setQuantity(3);
        em.persist(firstAmericano);
        em.persist(firstLatte);
        em.persist(secondAmericano);
        tx.commit();

        em.clear();
        tx.begin();
        Order savedFirstOrder = em.find(Order.class, firstOrder.getOrderId());
        Coffee savedAmericano = em.find(Coffee.class, americano.getCoffeeId());
        assertEquals(2, savedFirstOrder.getOrderCoffees().size());
        assertEquals(2, savedFirstOrder.getMember().getOrders().size());
        assertEquals(2, savedAmericano.getOrderCoffees().size());
        assertEquals(1, em.find(Coffee.class, latte.getCoffeeId()).getOrderCoffees().size());
        assertEquals(2, em.find(OrderCoffee.class, firstAmericano.getOrderCoffeeId()).getQuantity());
        assertEquals(1, em.find(OrderCoffee.class, firstLatte.getOrderCoffeeId()).getQuantity());
        assertEquals(3, em.find(OrderCoffee.class, secondAmericano.getOrderCoffeeId()).getQuantity());
        assertEquals(secondOrder.getOrderId(),
                em.find(OrderCoffee.class, secondAmericano.getOrderCoffeeId()).getOrder().getOrderId());
        assertEquals(americano.getCoffeeId(),
                em.find(OrderCoffee.class, secondAmericano.getOrderCoffeeId()).getCoffee().getCoffeeId());
        tx.commit();
    }

    /** 반대편 컬렉션만 채웠을 때와 외래 키를 관리하는 필드까지 설정했을 때를 비교한다. */
    @Test
    void inverseSideAloneDoesNotWriteForeignKey() {
        tx.begin();
        Member member = new Member("owner@cat.cat", "주인확인", "010-2222-2222");
        Order order = new Order();
        member.getOrders().add(order);
        em.persist(member);
        em.persist(order);
        tx.commit();

        em.clear();
        tx.begin();
        Member savedMember = em.find(Member.class, member.getMemberId());
        Order savedOrder = em.find(Order.class, order.getOrderId());
        assertNull(savedOrder.getMember());
        assertTrue(savedMember.getOrders().isEmpty());
        savedMember.addOrder(savedOrder); // 주인인 Order.member를 함께 바꾼다.
        tx.commit();

        em.clear();
        tx.begin();
        Order fixedOrder = em.find(Order.class, order.getOrderId());
        assertEquals(member.getMemberId(), fixedOrder.getMember().getMemberId());
        assertEquals(1, fixedOrder.getMember().getOrders().size());
        tx.commit();
    }

    /** 저장 전 신규 객체의 중복 호출과 관계 변경을 확인한다. 저장된 주문 재배정은 orphanRemoval과 충돌할 수 있다. */
    @Test
    void convenienceMethodsAvoidDuplicatesAndMoveRelations() {
        tx.begin();
        Member firstMember = new Member("first@cat.cat", "첫회원", "010-3333-3333");
        Member secondMember = new Member("second@cat.cat", "둘째회원", "010-4444-4444");
        Coffee firstCoffee = new Coffee("아메리카노", "Americano", 3500, "ARC");
        Coffee secondCoffee = new Coffee("카페 라테", "Caffe Latte", 4500, "CLT");
        em.persist(firstMember);
        em.persist(secondMember);
        em.persist(firstCoffee);
        em.persist(secondCoffee);
        Order firstOrder = new Order();
        Order secondOrder = new Order();
        firstMember.addOrder(firstOrder);
        firstMember.addOrder(firstOrder);
        secondMember.addOrder(firstOrder); // 기존 회원의 orders에서도 제거되어야 한다.
        secondMember.addOrder(secondOrder);
        assertTrue(firstMember.getOrders().isEmpty());
        assertEquals(2, secondMember.getOrders().size());
        em.persist(firstOrder);
        em.persist(secondOrder);

        OrderCoffee item = new OrderCoffee();
        item.setQuantity(1);
        firstOrder.addOrderCoffee(item);
        firstOrder.addOrderCoffee(item);
        firstCoffee.addOrderCoffee(item);
        firstCoffee.addOrderCoffee(item);
        assertEquals(1, firstOrder.getOrderCoffees().size());
        assertEquals(1, firstCoffee.getOrderCoffees().size());
        secondOrder.addOrderCoffee(item);
        secondCoffee.addOrderCoffee(item);
        assertTrue(firstOrder.getOrderCoffees().isEmpty());
        assertTrue(firstCoffee.getOrderCoffees().isEmpty());
        assertEquals(1, secondOrder.getOrderCoffees().size());
        assertEquals(1, secondCoffee.getOrderCoffees().size());
        em.persist(item);
        tx.commit();

        em.clear();
        tx.begin();
        OrderCoffee savedItem = em.find(OrderCoffee.class, item.getOrderCoffeeId());
        assertEquals(secondOrder.getOrderId(), savedItem.getOrder().getOrderId());
        assertEquals(secondCoffee.getCoffeeId(), savedItem.getCoffee().getCoffeeId());
        assertEquals(secondMember.getMemberId(), savedItem.getOrder().getMember().getMemberId());
        assertTrue(em.find(Member.class, firstMember.getMemberId()).getOrders().isEmpty());
        assertTrue(em.find(Order.class, firstOrder.getOrderId()).getOrderCoffees().isEmpty());
        assertTrue(em.find(Coffee.class, firstCoffee.getCoffeeId()).getOrderCoffees().isEmpty());
        tx.commit();
    }
}
