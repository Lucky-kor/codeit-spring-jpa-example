package com.codeit.codeitspringjpaexample.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 커서 응답: 다음 묶음이 있을 때만 마지막 반환 회원의 ID를 nextCursor로 제공한다. */
@AllArgsConstructor
@Getter
public class CursorResponseDto<T> {
    private final List<T> data; // 요청한 크기 이하의 실제 데이터. 확인용으로 더 읽은 한 건은 포함하지 않는다.
    private final boolean hasNext;
    private final Long nextCursor; // ID와 같은 Long. 마지막/빈 묶음은 null이므로 더 요청하지 않는다.
}
