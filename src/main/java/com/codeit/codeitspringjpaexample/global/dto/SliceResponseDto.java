package com.codeit.codeitspringjpaexample.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** Slice는 전체 건수/총 페이지 대신 현재 데이터와 다음 묶음 여부만 반환한다. */
@AllArgsConstructor
@Getter
public class SliceResponseDto<T> {
    private final List<T> data;
    private final boolean hasNext;
}
