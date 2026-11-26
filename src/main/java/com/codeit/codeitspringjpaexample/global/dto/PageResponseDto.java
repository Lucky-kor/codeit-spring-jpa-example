package com.codeit.codeitspringjpaexample.global.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/** Page의 엔티티 목록을 DTO 목록으로 바꾸고 페이지 정보만 추려 반환한다. */
@Getter
public class PageResponseDto<T> {
    private final List<T> data;
    private final PageInfo pageInfo;

    public PageResponseDto(List<T> data, Page<?> page) {
        this.data = data;
        // Spring Data의 0부터 시작하는 번호를 클라이언트가 사용하는 1부터 시작하는 번호로 돌린다.
        this.pageInfo = new PageInfo(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
