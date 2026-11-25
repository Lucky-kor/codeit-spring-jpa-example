package com.codeit.codeitspringjpaexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 실행 시작점. main → 스프링 초기화 → 엔티티/DB 준비 → 선택한 CommandLineRunner 순서로 진행한다.
 * README의 study.lesson / study.example 값으로 복습할 예제를 선택한다.
 */
@SpringBootApplication
public class CodeitSpringJpaExampleApplication {

    public static void main(String[] args) {
        // 프로그램 인자를 스프링 설정으로 전달한다. 예: --study.lesson=basic --study.example=01
        SpringApplication.run(CodeitSpringJpaExampleApplication.class, args);
    }

}
