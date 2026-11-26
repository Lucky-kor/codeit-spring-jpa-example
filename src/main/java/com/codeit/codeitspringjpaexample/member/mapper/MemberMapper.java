package com.codeit.codeitspringjpaexample.member.mapper;

import com.codeit.codeitspringjpaexample.member.dto.MemberDto;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct가 컴파일할 때 변환 구현체를 생성하고 스프링 빈으로 등록한다.
 * 필드명이 같은 email/name/phone은 자동 대응한다. DTO 변환은 DB 조회를 대신하지 않는다.
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {
    // 요청에는 없는 ID/관계/시각을 매핑하지 않아 새 엔티티의 기본값을 유지한다.
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "memberStatus", ignore = true)
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    Member memberPostDtoToMember(MemberDto.POST dto);

    // 응답에 필요한 기본 필드만 복사한다. 시각은 기존 응답 설계대로 문자열로 변환한다.
    MemberDto.RESPONSE memberToMemberResponseDto(Member member);

    // 같은 단일 변환을 목록의 각 회원에 적용한다.
    List<MemberDto.RESPONSE> membersToMemberResponseDtos(List<Member> members);
}
