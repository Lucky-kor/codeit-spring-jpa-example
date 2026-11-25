package com.codeit.codeitspringjpaexample.order.test;

import com.codeit.codeitspringjpaexample.coffee.entity.Coffee;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import com.codeit.codeitspringjpaexample.order.entity.OrderCoffee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [복습 4] 엔티티를 읽은 뒤, 이 파일의 예제를 enum → many-to-one → one-to-many → owner → many-to-many 순으로 실행한다.
 * 실행할 예제는 study.example 값으로 고른다. 전체 명령과 예상 결과는 README.md를 참고한다.
 * 이름에 Test가 있지만 자동 테스트 클래스가 아니라, 애플리케이션 시작 직후 실행하는 실습 코드다.
 */
@Configuration
@ConditionalOnProperty(name = "study.lesson", havingValue = "relations", matchIfMissing = true)
public class OrderTestRunner {

    // 다른 단원의 Runner와 이름이 겹치지 않게 등록한다. study.lesson으로 한 단원만 선택한다.
    @Bean
    public CommandLineRunner orderMappingRunner(EntityManagerFactory emFactory,
            @Value("${study.example:many-to-many}") String example) {
        return args -> {
            // EntityManagerFactory는 주입받고, 이번 실습에 쓸 영속성 컨텍스트만 직접 생성한다.
            EntityManager em = emFactory.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                System.out.println("\n[연관관계 실습] " + example);
                switch (example) {
                    case "enum" -> enumTestCase(em, tx);
                    case "many-to-one" -> mappingManyToOneTest(em, tx);
                    case "one-to-many" -> mappingOneToManyTest(em, tx);
                    case "owner" -> mappingOwnerTest(em, tx);
                    case "many-to-many" -> mappingManyToManyTest(em, tx);
                    default -> throw new IllegalArgumentException(
                            "study.example은 enum, many-to-one, one-to-many, owner, many-to-many 중에서 선택하세요.");
                }
            } catch (RuntimeException e) {
                // 실패한 트랜잭션을 남기지 않는다. 커밋 중 오류가 나면 이미 종료됐을 수도 있어 상태를 확인한다.
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                // 직접 생성한 EntityManager는 성공 여부와 관계없이 닫는다.
                em.close();
            }
        };
    }

    /** [4-1] enum 이름을 문자열로 저장한다. DB의 ORDER_STATUS와 Java enum을 비교한다. */
    private void enumTestCase(EntityManager em, EntityTransaction tx) {
        tx.begin();
        Order order = new Order(); // 기본 상태는 ORDER_REQUEST다.
        em.persist(order);
        tx.commit();

        // clear 이후에는 같은 객체를 그대로 출력하는 대신 DB에 저장된 값을 다시 읽게 된다.
        em.clear();
        tx.begin();
        Order findOrder = em.find(Order.class, order.getOrderId());
        System.out.println("[enum] 저장된 주문 상태 = " + findOrder.getOrderStatus()); // ORDER_REQUEST
        // @Enumerated(STRING)은 stepNumber(1)나 설명(결제 완료)이 아니라 ORDER_REQUEST를 저장한다.
        tx.commit();
    }

    /**
     * [4-2] 주문 → 회원 한 방향으로 연결하고 탐색하는 최소 예제다.
     * 최종 엔티티에는 반대편 컬렉션도 있으므로, 구조 자체를 단방향으로 되돌리는 예제는 아니다.
     * 단방향 매핑만 복습할 때는 Order.member의 @ManyToOne과 @JoinColumn부터 살펴본다.
     */
    private void mappingManyToOneTest(EntityManager em, EntityTransaction tx) {
        tx.begin();
        Member member = new Member("lucky@cat.cat", "luckyKim", "010-1234-1234");
        em.persist(member); // ① 먼저 참조할 회원을 저장한다.

        Order order = new Order();
        order.setMember(member); // ② 외래 키를 관리하는 Order.member를 설정한다.
        em.persist(order); // ③ 주문을 저장하면 MEMBER_ID가 기록된다.
        System.out.println("[단방향 접근] 현재 객체의 회원.orders 크기 = " + member.getOrders().size()); // 0
        // setMember는 일반 setter이므로 Member.orders까지 자동으로 채워 주지 않는다.
        tx.commit();

        em.clear();
        tx.begin();
        Order findOrder = em.find(Order.class, order.getOrderId());
        System.out.println("[단방향 접근] DB에서 다시 읽은 주문의 회원 = " + findOrder.getMember().getName());
        System.out.println("[단방향 접근] DB에서 다시 읽은 회원.orders 크기 = "
                + findOrder.getMember().getOrders().size()); // 최종 양방향 매핑에 따라 1
        tx.commit();
    }

    /** [4-3] 같은 외래 키를 기준으로 회원 ↔ 주문 양쪽에서 탐색한다. */
    private void mappingOneToManyTest(EntityManager em, EntityTransaction tx) {
        tx.begin();
        Member member = new Member("lucky@cat.cat", "luckyKim", "010-1234-1234");
        em.persist(member);
        Order order = new Order();
        member.addOrder(order); // ① 편의 메서드가 member.orders와 order.member를 함께 설정한다.
        em.persist(order); // ② 관계를 연결하는 것과 엔티티를 저장하는 것은 별도 작업이다.
        System.out.println("[양방향] 저장 전 객체의 회원.orders 크기 = " + member.getOrders().size()); // 1
        tx.commit();

        em.clear(); // ③ 메모리의 연결만 보고 성공했다고 판단하지 않도록 비운 뒤 재조회한다.
        tx.begin();
        Order findOrder = em.find(Order.class, order.getOrderId());
        Member findMember = em.find(Member.class, member.getMemberId());
        System.out.println("[양방향] 주문 → 회원 이름 = " + findOrder.getMember().getName()); // luckyKim
        System.out.println("[양방향] 회원 → 주문 개수 = " + findMember.getOrders().size()); // 1
        tx.commit();
    }

    /** [4-4] mappedBy가 있는 쪽만 수정하면 외래 키가 저장되지 않는다는 것을 비교한다. */
    private void mappingOwnerTest(EntityManager em, EntityTransaction tx) {
        tx.begin();
        Member member = new Member("lucky@cat.cat", "luckyKim", "010-1234-1234");
        Order order = new Order();
        member.getOrders().add(order); // ① 일부러 반대편 컬렉션만 변경한다. order.member는 여전히 null이다.
        em.persist(member);
        em.persist(order);
        System.out.println("[주인 비교] 메모리의 회원.orders 크기 = " + member.getOrders().size()); // 1
        tx.commit();

        em.clear();
        tx.begin();
        Member findMember = em.find(Member.class, member.getMemberId());
        Order findOrder = em.find(Order.class, order.getOrderId());
        System.out.println("[주인 비교] 재조회한 주문.member가 null인가? " + (findOrder.getMember() == null)); // true
        System.out.println("[주인 비교] 재조회한 회원.orders 크기 = " + findMember.getOrders().size()); // 0

        // ② 주인인 order.member도 함께 설정하면 UPDATE로 MEMBER_ID가 반영된다.
        findMember.addOrder(findOrder);
        tx.commit();

        em.clear();
        tx.begin();
        Order fixedOrder = em.find(Order.class, order.getOrderId());
        System.out.println("[주인 비교] 편의 메서드 적용 후 회원 이름 = " + fixedOrder.getMember().getName());
        System.out.println("[주인 비교] 편의 메서드 적용 후 주문 개수 = "
                + fixedOrder.getMember().getOrders().size()); // 1
        tx.commit();
    }

    /**
     * [4-5] 주문과 커피 사이의 다대다 관계를 OrderCoffee라는 중간 엔티티로 풀어 저장한다.
     * Order ↔ OrderCoffee, Coffee ↔ OrderCoffee 두 양방향 관계를 사용하며, 주문별 수량도 저장한다.
     */
    private void mappingManyToManyTest(EntityManager em, EntityTransaction tx) {
        tx.begin();
        // ① 참조될 부모 엔티티를 먼저 저장한다. 새 객체를 필드에 넣는 것만으로 DB에 저장되지는 않는다.
        Member member = new Member("lucky@cat.cat", "luckyKim", "010-1234-1234");
        Coffee coffee1 = new Coffee("아메리카노", "Americano", 3500, "ARC");
        Coffee coffee2 = new Coffee("카페 라테", "Caffe Latte", 4500, "CLT");
        em.persist(member);
        em.persist(coffee1);
        em.persist(coffee2);

        Order order = new Order();
        order.addMember(member);
        em.persist(order);

        // ② '어떤 주문에 어떤 커피가 몇 잔 들어가는가'를 주문항목 한 행으로 표현한다.
        OrderCoffee orderCoffee1 = new OrderCoffee();
        orderCoffee1.addOrder(order); // OrderCoffee.order와 Order.orderCoffees를 함께 연결
        orderCoffee1.addCoffee(coffee1); // OrderCoffee.coffee와 Coffee.orderCoffees를 함께 연결
        orderCoffee1.setQuantity(2);

        OrderCoffee orderCoffee2 = new OrderCoffee();
        orderCoffee2.addOrder(order);
        orderCoffee2.addCoffee(coffee2);
        orderCoffee2.setQuantity(1);

        // ③ 부모가 저장된 뒤 주문항목을 각각 저장한다. ORDER_ID, COFFEE_ID, QUANTITY를 관찰한다.
        em.persist(orderCoffee1);
        em.persist(orderCoffee2);
        tx.commit();

        // ④ 반드시 비우고 다시 읽어 외래 키와 수량이 실제 DB에 저장되었는지 확인한다.
        em.clear();
        tx.begin();
        Order findOrder = em.find(Order.class, order.getOrderId());
        System.out.println("[다대다] 주문한 회원 = " + findOrder.getMember().getName());
        System.out.println("[다대다] 주문항목 개수 = " + findOrder.getOrderCoffees().size()); // 2
        for (OrderCoffee item : findOrder.getOrderCoffees()) {
            // 컬렉션의 출력 순서는 보장하지 않는다. 커피 이름과 수량의 짝을 확인한다.
            System.out.println("[다대다] " + item.getCoffee().getKorName() + " / " + item.getQuantity() + "잔");
        }
        Coffee findCoffee = em.find(Coffee.class, coffee1.getCoffeeId());
        System.out.println("[다대다] 아메리카노 → 주문항목 개수 = " + findCoffee.getOrderCoffees().size()); // 1
        for (OrderCoffee item : findCoffee.getOrderCoffees()) {
            System.out.println("[다대다] 커피 → 주문항목 → 주문 ID = " + item.getOrder().getOrderId());
        }
        // 복습 과제: 같은 coffee1을 담은 두 번째 Order/OrderCoffee를 저장하고, 역방향 항목 수가 2인지 확인한다.
        tx.commit();
    }
}
