package com.codeit.codeitspringjpaexample.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Page에서 제공하는 전체 건수와 총 페이지 수. Slice/커서 응답에는 이 정보가 없다. */
@AllArgsConstructor
@Getter
public class PageInfo {
    private final int page; // API의 페이지 번호는 1부터 시작한다.
    private final int size; // 요청한 페이지 크기. 마지막 페이지의 실제 데이터 수와 다를 수 있다.
    private final long totalElements; // 전체 조회 결과의 개수.
    private final int totalPages;
}
