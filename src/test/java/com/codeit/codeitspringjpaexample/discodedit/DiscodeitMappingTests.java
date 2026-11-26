package com.codeit.codeitspringjpaexample.discodedit;

import com.codeit.codeitspringjpaexample.discodedit.entity.Channel;
import com.codeit.codeitspringjpaexample.discodedit.entity.ReadStatus;
import com.codeit.codeitspringjpaexample.discodedit.entity.User;
import com.codeit.codeitspringjpaexample.discodedit.entity.UserStatus;
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
 * Discodeit 복습 결과를 H2 DB에서 검증한다. Runner와 PostgreSQL용 초기 SQL은 실행하지 않는다.
 * 먼저 Runner를 따라 작성하고, 이 테스트의 commit → clear → 재조회를 결과 확인 방법으로 읽는다.
 */
@SpringBootTest(properties = {"study.lesson=none", "spring.sql.init.mode=never"})
@ActiveProfiles("local")
class DiscodeitMappingTests {
    @Autowired
    private EntityManagerFactory factory;

    private EntityManager em;
    private EntityTransaction tx;

    @BeforeEach
    void openEntityManager() {
        em = factory.createEntityManager();
        tx = em.getTransaction();
    }

    @AfterEach
    void closeEntityManager() {
        if (tx.isActive()) {
            tx.rollback();
        }
        em.close();
    }

    /** 사용자 두 명/채널 두 개/읽음 상태 세 개와 사용자 상태가 실제 FK로 양쪽에서 조회되어야 한다. */
    @Test
    void persistsManyToManyAndOneToOneRelations() {
        tx.begin();
        User first = user("매핑 첫 사용자");
        User second = user("매핑 둘째 사용자");
        Channel java = channel("mapping-java");
        Channel spring = channel("mapping-spring");
        em.persist(first);
        em.persist(second);
        em.persist(java);
        em.persist(spring);
        ReadStatus firstJava = readStatus(first, java);
        ReadStatus firstSpring = readStatus(first, spring);
        ReadStatus secondJava = readStatus(second, java);
        em.persist(firstJava);
        em.persist(firstSpring);
        em.persist(secondJava);
        UserStatus status = new UserStatus();
        first.setUserStatus(status);
        em.persist(status);
        tx.commit();

        em.clear();
        tx.begin();
        User savedFirst = em.find(User.class, first.getUserId());
        assertEquals(2, savedFirst.getReadStatuses().size());
        assertEquals(1, em.find(User.class, second.getUserId()).getReadStatuses().size());
        assertEquals(2, em.find(Channel.class, java.getChannelId()).getReadStatuses().size());
        assertEquals(1, em.find(Channel.class, spring.getChannelId()).getReadStatuses().size());
        ReadStatus savedLink = em.find(ReadStatus.class, firstSpring.getReadStatusId());
        assertEquals(first.getUserId(), savedLink.getUser().getUserId());
        assertEquals(spring.getChannelId(), savedLink.getChannel().getChannelId());
        assertEquals(status.getUserStatusId(), savedFirst.getUserStatus().getUserStatusId());
        assertEquals(first.getUserId(), em.find(UserStatus.class, status.getUserStatusId()).getUser().getUserId());
        tx.commit();
    }

    /** 중복을 막고 사용자/채널 변경과 null 해제를 DB 재조회로 확인한다. */
    @Test
    void avoidsDuplicateLinksAndPersistsChangedOrClearedForeignKeys() {
        tx.begin();
        User first = user("이동 전 사용자");
        User second = user("이동 후 사용자");
        Channel oldChannel = channel("move-old");
        Channel newChannel = channel("move-new");
        em.persist(first);
        em.persist(second);
        em.persist(oldChannel);
        em.persist(newChannel);
        ReadStatus link = readStatus(first, oldChannel);
        first.addReadStatus(link);
        first.setReadStatuses(link); // 기존 메서드도 같은 편의 메서드에 위임한다.
        oldChannel.addReadStatus(link);
        oldChannel.setReadStatuses(link);
        assertEquals(1, first.getReadStatuses().size());
        assertEquals(1, oldChannel.getReadStatuses().size());
        em.persist(link);
        tx.commit();

        em.clear();
        tx.begin();
        ReadStatus savedLink = em.find(ReadStatus.class, link.getReadStatusId());
        User savedFirst = em.find(User.class, first.getUserId());
        Channel savedOldChannel = em.find(Channel.class, oldChannel.getChannelId());
        em.find(User.class, second.getUserId()).addReadStatus(savedLink);
        em.find(Channel.class, newChannel.getChannelId()).addReadStatus(savedLink);
        assertTrue(savedFirst.getReadStatuses().isEmpty());
        assertTrue(savedOldChannel.getReadStatuses().isEmpty());
        tx.commit();

        em.clear();
        tx.begin();
        ReadStatus moved = em.find(ReadStatus.class, link.getReadStatusId());
        assertEquals(second.getUserId(), moved.getUser().getUserId());
        assertEquals(newChannel.getChannelId(), moved.getChannel().getChannelId());
        assertTrue(em.find(User.class, first.getUserId()).getReadStatuses().isEmpty());
        assertTrue(em.find(Channel.class, oldChannel.getChannelId()).getReadStatuses().isEmpty());
        moved.setUser(null);
        moved.setChannel(null);
        tx.commit();

        em.clear();
        tx.begin();
        ReadStatus unlinked = em.find(ReadStatus.class, link.getReadStatusId());
        assertNotNull(unlinked); // 연결 해제는 행 삭제와 다르다.
        assertNull(unlinked.getUser());
        assertNull(unlinked.getChannel());
        assertTrue(em.find(User.class, second.getUserId()).getReadStatuses().isEmpty());
        assertTrue(em.find(Channel.class, newChannel.getChannelId()).getReadStatuses().isEmpty());
        tx.commit();
    }

    /** 일대일 관계도 사용자 변경/반대쪽에서의 해제 후 주인 FK가 갱신되고 상태 행은 남아야 한다. */
    @Test
    void changesAndClearsOneToOneRelationFromBothSides() {
        tx.begin();
        User first = user("일대일 이전");
        User second = user("일대일 이후");
        em.persist(first);
        em.persist(second);
        UserStatus status = new UserStatus();
        first.setUserStatus(status);
        first.setUserStatus(status); // 같은 상태를 다시 연결해도 재귀 없이 종료한다.
        em.persist(status);
        tx.commit();

        em.clear();
        tx.begin();
        UserStatus savedStatus = em.find(UserStatus.class, status.getUserStatusId());
        User savedFirst = em.find(User.class, first.getUserId());
        User savedSecond = em.find(User.class, second.getUserId());
        savedStatus.setUser(savedSecond);
        assertNull(savedFirst.getUserStatus());
        assertSame(savedStatus, savedSecond.getUserStatus());
        tx.commit();

        em.clear();
        tx.begin();
        User secondWithStatus = em.find(User.class, second.getUserId());
        assertNull(em.find(User.class, first.getUserId()).getUserStatus());
        assertEquals(status.getUserStatusId(), secondWithStatus.getUserStatus().getUserStatusId());
        secondWithStatus.setUserStatus(null); // 반대쪽 편의 메서드도 주인 UserStatus.user까지 해제한다.
        tx.commit();

        em.clear();
        tx.begin();
        UserStatus unlinked = em.find(UserStatus.class, status.getUserStatusId());
        assertNotNull(unlinked);
        assertNull(unlinked.getUser());
        assertNull(em.find(User.class, second.getUserId()).getUserStatus());
        tx.commit();
    }

    // 아래 메서드는 테스트의 준비 코드 중복만 줄인다. 엔티티 저장은 각 테스트에서 명시한다.
    private User user(String name) {
        User user = new User();
        user.setUsername(name);
        return user;
    }

    private Channel channel(String name) {
        Channel channel = new Channel();
        channel.setChannelName(name);
        return channel;
    }

    private ReadStatus readStatus(User user, Channel channel) {
        ReadStatus status = new ReadStatus();
        status.setUser(user);
        status.setChannel(channel);
        return status;
    }
}
