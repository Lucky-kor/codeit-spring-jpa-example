package com.codeit.codeitspringjpaexample.member;

import com.codeit.codeitspringjpaexample.member.repository.MemberRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * H2와 실제 HTTP 서버에서 커서 계약을 검증한다. PostgreSQL이나 미리 넣어 둔 SQL 파일은 필요 없다.
 * HTTP 요청은 별도 트랜잭션으로 실행되므로 각 테스트 전에 데이터를 명시적으로 정리한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "study.lesson=none", "spring.sql.init.mode=never", "spring.jpa.properties.hibernate.generate_statistics=true"
})
@ActiveProfiles("local")
class MemberApiTests {
    private static final long LARGE_ID = 3_000_000_000L; // Integer 범위를 넘어도 ID/커서가 잘리지 않아야 한다.
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManagerFactory emFactory;
    @Autowired
    private MemberRepository memberRepository;
    private RestClient client;

    @BeforeEach
    void prepareMembers() {
        client = RestClient.create("http://localhost:" + port + "/v11/members");
        clearMembers();
        long[] ids = {1L, 2L, 3L, 4L, 5L, LARGE_ID};
        for (int i = 0; i < ids.length; i++) {
            jdbc.update("INSERT INTO members(member_id,email,name,phone,created_at,modified_at) "
                            + "VALUES(?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    ids[i], "cursor" + i + "@example.com", "회원" + i, String.format("010-0000-%04d", i));
        }
    }

    /** 첫 요청은 cursor 없이, 다음 요청은 직전 nextCursor로 이어 간다. 마지막 응답에는 커서가 없다. */
    @Test
    void cursorTraversesFirstMiddleAndLastSlicesWithoutDuplicates() {
        Map<String, Object> first = get("/cursor?size=2").getBody();
        assertEquals(List.of(LARGE_ID, 5L), ids(first));
        assertEquals(true, first.get("hasNext"));
        assertEquals(5L, ((Number) first.get("nextCursor")).longValue());

        Map<String, Object> middle = get("/cursor?size=2&cursor=" + first.get("nextCursor")).getBody();
        assertEquals(List.of(4L, 3L), ids(middle));
        assertEquals(true, middle.get("hasNext"));
        assertEquals(3L, ((Number) middle.get("nextCursor")).longValue());

        Map<String, Object> last = get("/cursor?size=2&cursor=" + middle.get("nextCursor")).getBody();
        assertEquals(List.of(2L, 1L), ids(last));
        assertEquals(false, last.get("hasNext"));
        assertTrue(last.containsKey("nextCursor"));
        assertNull(last.get("nextCursor"));
    }

    /** 커서가 가리키는 회원 행 자체가 삭제되어도 ID 비교 조건으로 다음 묶음을 이어 간다. */
    @Test
    void cursorContinuesWhenBoundaryMemberWasDeletedBetweenRequests() {
        Map<String, Object> first = get("/cursor?size=2").getBody();
        long cursor = ((Number) first.get("nextCursor")).longValue();
        assertEquals(204, client.delete().uri("/" + cursor).retrieve().toBodilessEntity().getStatusCode().value());
        Map<String, Object> next = get("/cursor?size=2&cursor=" + cursor).getBody();
        assertEquals(List.of(4L, 3L), ids(next));
        assertEquals(true, next.get("hasNext"));
    }

    /** 큰 Long 커서를 요청으로 넘겨도 다음 회원을 정확히 찾는다. */
    @Test
    void cursorAcceptsLongIdsBeyondIntegerRange() {
        Map<String, Object> first = get("/cursor?size=1").getBody();
        assertEquals(List.of(LARGE_ID), ids(first));
        assertEquals(LARGE_ID, ((Number) first.get("nextCursor")).longValue());
        Map<String, Object> next = get("/cursor?size=1&cursor=" + LARGE_ID).getBody();
        assertEquals(List.of(5L), ids(next));
    }

    /** 조건에 맞는 회원이 없거나 DB 자체가 비어 있으면 빈 목록/false/null을 반환한다. */
    @Test
    void emptyCursorResultsHaveNoNextCursor() {
        Map<String, Object> afterLast = get("/cursor?size=3&cursor=1").getBody();
        assertEquals(List.of(), ids(afterLast));
        assertEquals(false, afterLast.get("hasNext"));
        assertNull(afterLast.get("nextCursor"));
        clearMembers();
        Map<String, Object> empty = get("/cursor?size=3").getBody();
        assertEquals(List.of(), ids(empty));
        assertEquals(false, empty.get("hasNext"));
        assertNull(empty.get("nextCursor"));
    }

    /** Slice/커서는 전체 count를 하지 않고 Page는 전체 건수를 제공하는지 실제 실행 쿼리를 비교한다. */
    @Test
    void sliceAndCursorAvoidCountWhilePageProvidesTotals() {
        Statistics statistics = emFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        Map<String, Object> page = get("/page?page=1&size=1").getBody();
        assertEquals(6L, ((Number) ((Map<?, ?>) page.get("pageInfo")).get("totalElements")).longValue());
        assertTrue(hasCountQuery(statistics), "Page의 count 쿼리가 관찰되어야 비교가 유효하다.");

        statistics.clear();
        Map<String, Object> slice = get("?page=1&size=1").getBody();
        assertEquals(List.of(LARGE_ID), ids(slice));
        assertEquals(true, slice.get("hasNext"));
        assertFalse(slice.containsKey("pageInfo"));
        assertFalse(hasCountQuery(statistics));

        statistics.clear();
        get("/cursor?size=1");
        assertFalse(hasCountQuery(statistics));
    }

    /** 범위 밖의 크기/페이지/커서와 숫자가 아닌 커서는 서버 오류 대신 400으로 거절한다. */
    @Test
    void invalidPagingInputsReturnBadRequest() {
        for (String path : List.of("/cursor?size=0", "/cursor?size=101", "/cursor?cursor=-1",
                "/cursor?cursor=0", "/cursor?cursor=abc", "/cursor?cursor=9223372036854775808",
                "?page=0", "/page?page=0", "/page?size=-1")) {
            assertEquals(400, get(path).getStatusCode().value(), path);
        }
    }

    /** POST 역직렬화/검증과 응답 ID 타입을 함께 확인한다. 동일 이메일은 409다. */
    @Test
    void postDeserializesValidatesAndRejectsDuplicateEmail() {
        clearMembers();
        String body = "{\"email\":\"post@example.com\",\"name\":\"생성회원\",\"phone\":\"010-1234-5678\"}";
        ResponseEntity<Map<String, Object>> created = post(body);
        assertEquals(201, created.getStatusCode().value());
        assertInstanceOf(Number.class, created.getBody().get("memberId"));
        assertEquals("post@example.com", created.getBody().get("email"));
        assertNotNull(created.getHeaders().getLocation());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM member_status", Integer.class));
        assertEquals(409, post(body).getStatusCode().value());
        assertEquals(400, post("{\"email\":\"bad\",\"name\":\"\",\"phone\":\"1\"}").getStatusCode().value());
    }

    /** 파생 쿼리와 JPQL/native 쿼리가 같은 회원을 찾는지 확인한다. */
    @Test
    void jpqlAndNativeQueriesFindTheSameMember() {
        assertEquals(3L, memberRepository.findQueryByUsername("회원2").get(0).getMemberId());
        assertEquals(3L, memberRepository.findNativeQueryByUsername("회원2").get(0).getMemberId());
        assertEquals(3L, memberRepository.findByEmail("cursor2@example.com").orElseThrow().getMemberId());
    }

    // 서버가 만든 4xx 본문도 읽어 상태 코드를 검사하기 위해 기본 예외 처리를 이 테스트에서만 끈다.
    private ResponseEntity<Map<String, Object>> get(String path) {
        return client.get().uri(path).retrieve().onStatus(status -> status.isError(), (request, response) -> {})
                .toEntity(JSON_OBJECT);
    }

    private ResponseEntity<Map<String, Object>> post(String body) {
        return client.post().contentType(MediaType.APPLICATION_JSON).body(body).retrieve()
                .onStatus(status -> status.isError(), (request, response) -> {}).toEntity(JSON_OBJECT);
    }

    @SuppressWarnings("unchecked")
    private List<Long> ids(Map<String, Object> response) {
        return ((List<Map<String, Object>>) response.get("data")).stream()
                .map(member -> ((Number) member.get("memberId")).longValue()).toList();
    }

    private boolean hasCountQuery(Statistics statistics) {
        return Arrays.stream(statistics.getQueries()).anyMatch(query -> query.toLowerCase().matches("(?s).*count\\s*\\(.*"));
    }

    private void clearMembers() {
        // 외래 키가 참조하는 자식부터 지운다. SQL 초기화/애플리케이션 시드에 의존하지 않는 테스트다.
        jdbc.update("DELETE FROM order_coffee");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM member_status");
        jdbc.update("DELETE FROM members");
    }
}
