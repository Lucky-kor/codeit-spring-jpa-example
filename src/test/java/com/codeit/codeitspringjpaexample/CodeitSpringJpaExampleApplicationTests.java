package com.codeit.codeitspringjpaexample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 기본 실행 점검: 외부 PostgreSQL 없이 H2에서 애플리케이션과 엔티티 매핑이 초기화되는지 확인한다.
 * study.lesson=none으로 시작 실습은 끈다. 연관관계의 저장 결과는 AssociationMappingTests에서 검증한다.
 */
@SpringBootTest(properties = "study.lesson=none")
@ActiveProfiles("local")
class CodeitSpringJpaExampleApplicationTests {

    @Test
    void contextLoads() {
        // @SpringBootTest가 초기화에 실패하면 이 메서드에 도달하기 전에 테스트가 실패한다.
    }

}
