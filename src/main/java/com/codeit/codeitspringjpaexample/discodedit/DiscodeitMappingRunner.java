package com.codeit.codeitspringjpaexample.discodedit;

import com.codeit.codeitspringjpaexample.discodedit.entity.Channel;
import com.codeit.codeitspringjpaexample.discodedit.entity.ReadStatus;
import com.codeit.codeitspringjpaexample.discodedit.entity.User;
import com.codeit.codeitspringjpaexample.discodedit.entity.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Discodeit으로 관계 매핑을 다시 연습한다. 먼저 entity의 네 클래스를 읽고 이 파일을 따라 작성한다.
 * --study.lesson=discodeit --study.example=mapping 또는 one-to-one으로 실행한다.
 * 별도 USERS/CHANNEL/READ_STATUS/USER_STATUS 테이블을 사용하여 회원 API의 초기 데이터와 구분한다.
 */
@Configuration
@ConditionalOnProperty(name = "study.lesson", havingValue = "discodeit")
public class DiscodeitMappingRunner {

    // 한 실행에 한 예제만 선택한다. 새 EntityManager의 생성/종료는 여기서 한 번 담당한다.
    @Bean
    public CommandLineRunner discodeitExampleRunner(EntityManagerFactory factory,
            @Value("${study.example:mapping}") String example) {
        return args -> {
            EntityManager em = factory.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                System.out.println("[discodeit] 선택한 예제: " + example);
                switch (example) {
                    case "mapping" -> mapping(em, tx);
                    case "one-to-one" -> oneToOne(em, tx);
                    default -> throw new IllegalArgumentException("discodeit 예제: mapping, one-to-one");
                }
            } finally {
                if (tx.isActive()) {
                    tx.rollback(); // 오류로 남은 트랜잭션은 취소한다.
                }
                em.close(); // 직접 생성한 EntityManager는 성공/실패 모두 닫는다.
            }
        };
    }

    /** 사용자 두 명과 채널 두 개를 읽음 상태 세 개로 연결하여 다대다의 양쪽을 재조회한다. */
    private void mapping(EntityManager em, EntityTransaction tx) {
        tx.begin();
        // ① 참조 대상부터 저장한다. cascade를 사용하지 않으므로 각 엔티티를 명시적으로 persist한다.
        User springUser = new User();
        springUser.setUsername("봄");
        User summerUser = new User();
        summerUser.setUsername("여름");
        Channel java = new Channel();
        java.setChannelName("java");
        Channel spring = new Channel();
        spring.setChannelName("spring");
        em.persist(springUser);
        em.persist(summerUser);
        em.persist(java);
        em.persist(spring);

        // ② 봄은 java/spring, 여름은 java에 연결한다. 두 방향에서 같은 관계를 탐색할 수 있다.
        ReadStatus springJava = new ReadStatus();
        springUser.addReadStatus(springJava); // User.readStatuses와 ReadStatus.user를 함께 설정한다.
        java.addReadStatus(springJava); // Channel.readStatuses와 ReadStatus.channel도 함께 설정한다.
        ReadStatus springSpring = new ReadStatus();
        springSpring.setUser(springUser); // 주인 쪽에서 출발해도 반대쪽 목록이 맞춰진다.
        springSpring.setChannel(spring);
        ReadStatus summerJava = new ReadStatus();
        summerJava.setUser(summerUser);
        summerJava.setChannel(java);
        em.persist(springJava);
        em.persist(springSpring);
        em.persist(summerJava);
        tx.commit();

        // ③ 기존 자바 객체만 관찰하지 않고 DB의 FK로 관계를 복원해 확인한다. ID는 하드코딩하지 않는다.
        em.clear();
        tx.begin();
        User savedSpring = em.find(User.class, springUser.getUserId());
        User savedSummer = em.find(User.class, summerUser.getUserId());
        Channel savedJava = em.find(Channel.class, java.getChannelId());
        Channel savedSpringChannel = em.find(Channel.class, spring.getChannelId());
        System.out.println("[discodeit] 봄의 채널 개수 = " + savedSpring.getReadStatuses().size()); // 2
        System.out.println("[discodeit] 여름의 채널 개수 = " + savedSummer.getReadStatuses().size()); // 1
        System.out.println("[discodeit] java의 사용자 개수 = " + savedJava.getReadStatuses().size()); // 2
        System.out.println("[discodeit] spring의 사용자 개수 = " + savedSpringChannel.getReadStatuses().size()); // 1
        for (ReadStatus status : savedSpring.getReadStatuses()) {
            System.out.println("[discodeit] 사용자 → 읽음 상태 → 채널 = " + status.getChannel().getChannelName());
        }
        for (ReadStatus status : savedJava.getReadStatuses()) {
            System.out.println("[discodeit] 채널 → 읽음 상태 → 사용자 = " + status.getUser().getUsername());
        }
        tx.commit();
    }

    /** UserStatus가 FK의 주인인 일대일 관계를 저장하고, 연결 해제와 행 삭제를 구분한다. */
    private void oneToOne(EntityManager em, EntityTransaction tx) {
        tx.begin();
        User user = new User();
        user.setUsername("상태 복습");
        em.persist(user);
        UserStatus status = new UserStatus();
        user.setUserStatus(status); // 반대쪽에서 출발해도 주인 UserStatus.user까지 연결된다.
        em.persist(status); // 객체 연결은 저장 명령이 아니므로 상태도 따로 저장한다.
        tx.commit();

        em.clear();
        tx.begin();
        User savedUser = em.find(User.class, user.getUserId());
        UserStatus savedStatus = em.find(UserStatus.class, status.getUserStatusId());
        System.out.println("[일대일] 사용자 → 상태 ID = " + savedUser.getUserStatus().getUserStatusId());
        System.out.println("[일대일] 상태 → 사용자 이름 = " + savedStatus.getUser().getUsername());
        // USER_STATUS.USER_ID만 null로 변경한다. USERS의 PK나 READ_STATUS.USER_ID와는 별개의 컬럼이다.
        savedStatus.setUser(null);
        System.out.println("[일대일] 연결 해제 직후 사용자.status는 null인가? " + (savedUser.getUserStatus() == null));
        tx.commit();

        em.clear();
        tx.begin();
        UserStatus detachedRelation = em.find(UserStatus.class, status.getUserStatusId());
        System.out.println("[일대일] 재조회한 상태 행은 남아 있는가? " + (detachedRelation != null)); // true
        System.out.println("[일대일] 재조회한 상태.user는 null인가? " + (detachedRelation.getUser() == null)); // true
        System.out.println("[일대일] 재조회한 사용자.status는 null인가? "
                + (em.find(User.class, user.getUserId()).getUserStatus() == null)); // true
        tx.commit();
    }
}
