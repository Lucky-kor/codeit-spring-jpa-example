package com.codeit.codeitspringjpaexample.member.mapper;

import com.codeit.codeitspringjpaexample.member.dto.MemberDto;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    // post dto to entity
    Member memberPostDtoToMember(MemberDto.POST dto);
    // entity to response dto
    MemberDto.RESPONSE memberToMemberResponseDto(Member member);
}
