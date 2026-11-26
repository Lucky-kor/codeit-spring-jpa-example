package com.codeit.codeitspringjpaexample.member.controller;

import com.codeit.codeitspringjpaexample.member.dto.MemberDto;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.mapper.MemberMapper;
import com.codeit.codeitspringjpaexample.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v11/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper mapper;

    @PostMapping
    public ResponseEntity postMember(@RequestBody MemberDto.POST dto) {
        // dto to entity 후 서비스에 전달
        Member member = mapper.memberPostDtoToMember(dto);
        Member createdMember = memberService.createMember(member);
        MemberDto.RESPONSE response = mapper.memberToMemberResponseDto(createdMember);

        URI location = URI.create("/v11/members/" + response.getMemberId());
//        return ResponseEntity.ok(response);
        return ResponseEntity.created(location).body(response);
    }
}
