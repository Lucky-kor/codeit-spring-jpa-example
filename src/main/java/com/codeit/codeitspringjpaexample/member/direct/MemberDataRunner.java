package com.codeit.codeitspringjpaexample.member.direct;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.entity.MemberStatus;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * API 단원에서만 사용하는 샘플 데이터 준비 코드다. H2/PostgreSQL에서 같은 예제를 실행한다.
 * data.sql의 특정 DB 문법 대신 앞 단원에서 배운 EntityManager와 persist를 그대로 사용한다.
 * api가 아닌 basic/mapping/relations/discodeit 실습과 자동 테스트의 데이터에는 관여하지 않는다.
 */
@Configuration
@ConditionalOnProperty(name = "study.lesson", havingValue = "api")
public class MemberDataRunner {

    @Bean
    public CommandLineRunner memberSeedRunner(EntityManagerFactory factory) {
        return args -> {
            EntityManager em = factory.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                // 실제 데이터가 있으면 덮어쓰지 않는다. ddl-auto=create인 학습 환경에서는 보통 0이다.
                Long count = em.createQuery("SELECT COUNT(m) FROM MEMBERS m", Long.class).getSingleResult();
                if (count > 0) {
                    System.out.println("[API 준비] 기존 회원이 있어 샘플 데이터 생성을 건너뜁니다.");
                    tx.commit();
                    return;
                }

                String[] names = {"김철수", "이영희", "박지민", "최유나", "정우성",
                        "한지민", "오세훈", "서지혜", "문지훈", "윤가영"};
                List<Member> members = new ArrayList<>();
                for (int i = 0; i < names.length; i++) {
                    Member member = new Member("member" + (i + 1) + "@example.com", names[i],
                            String.format("010-0000-%04d", i + 1));
                    // 객체 참조는 편의 메서드가 맞추고, 상태 INSERT는 cascade=PERSIST가 전파한다.
                    member.setMemberStatus(new MemberStatus());
                    em.persist(member);
                    members.add(member);
                }

                // 첫 회원은 주문 3건, 둘째 회원은 주문 2건. 나머지 회원은 주문이 없다.
                // N+1 비교에서 LEFT JOIN FETCH가 주문 없는 회원도 보존하는지 확인할 수 있다.
                Order.OrderStatus[] statuses = {Order.OrderStatus.ORDER_REQUEST,
                        Order.OrderStatus.ORDER_CONFIRM, Order.OrderStatus.ORDER_COMPLETE,
                        Order.OrderStatus.ORDER_REQUEST, Order.OrderStatus.ORDER_CANCEL};
                for (int i = 0; i < statuses.length; i++) {
                    Order order = new Order();
                    order.addMember(members.get(i < 3 ? 0 : 1));
                    order.setOrderStatus(statuses[i]);
                    em.persist(order); // Member.orders에는 PERSIST가 없으므로 주문은 직접 저장한다.
                }
                tx.commit();
                System.out.println("[API 준비] 회원 10명 / 회원 상태 10개 / 주문 5건을 저장했습니다.");
                System.out.println("[API 준비] /v11/members, /page, /cursor와 /test?mode=basic부터 비교하세요.");
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        };
    }
}
