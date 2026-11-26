package com.codeit.codeitspringjpaexample.member.controller;

import com.codeit.codeitspringjpaexample.global.dto.CursorResponseDto;
import com.codeit.codeitspringjpaexample.global.dto.PageResponseDto;
import com.codeit.codeitspringjpaexample.global.dto.SliceResponseDto;
import com.codeit.codeitspringjpaexample.member.dto.MemberDto;
import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.mapper.MemberMapper;
import com.codeit.codeitspringjpaexample.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * [API 실습 시작점] 요청 → Service → Repository → DTO 변환 → 응답 순서로 읽는다.
 * 엔티티를 그대로 JSON으로 반환하지 않아 양방향 관계가 끝없이 이어지는 일을 피한다.
 * page는 외부 요청에서 1부터, PageRequest에서는 0부터 센다. 모든 목록은 회원 ID 내림차순이다.
 * 매개변수의 제약은 Spring MVC의 기본 검증으로 처리되어 잘못된 입력에 400을 응답한다.
 */
@RestController
@RequestMapping("/v11/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper mapper;

    /** 같은 회원 목록을 조회하면서 basic/fetch/graph의 SQL을 비교한다. 주문 없는 회원도 포함한다. */
    @GetMapping("/test")
    public ResponseEntity<List<MemberDto.RESPONSE>> getTestMembers(
            @RequestParam(defaultValue = "basic") String mode) {
        List<Member> findMembers = memberService.findTestMembers(mode);
        return ResponseEntity.ok(mapper.membersToMemberResponseDtos(findMembers));
    }

    /** POST 본문을 검증하고 회원과 상태를 함께 저장한다. 응답의 Location에서 생성된 ID를 확인한다. */
    @PostMapping
    public ResponseEntity<MemberDto.RESPONSE> postMember(@Valid @RequestBody MemberDto.POST dto) {
        Member member = mapper.memberPostDtoToMember(dto);
        Member createdMember = memberService.createMember(member);
        MemberDto.RESPONSE response = mapper.memberToMemberResponseDto(createdMember);
        URI location = URI.create("/v11/members/" + response.getMemberId());
        return ResponseEntity.created(location).body(response);
    }

    /** 회원 삭제가 상태/주문/주문항목으로 전파되는 cascade REMOVE 실습이다. 커피 메뉴는 남는다. */
    @DeleteMapping("/{member-id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("member-id") @Positive Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    /** Page: data와 전체 건수/전체 페이지 수를 반환한다. count SQL이 필요한지 로그에서 확인한다. */
    @GetMapping("/page")
    public ResponseEntity<PageResponseDto<MemberDto.RESPONSE>> getMembersPage(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Page<Member> pageMembers = memberService.findMembers(page - 1, size);
        List<MemberDto.RESPONSE> responses = mapper.membersToMemberResponseDtos(pageMembers.getContent());
        return ResponseEntity.ok(new PageResponseDto<>(responses, pageMembers));
    }

    /** Slice: 기존 기본 경로를 유지한다. 전체 건수 대신 다음 묶음이 있는지만 알려 준다. */
    @GetMapping
    public ResponseEntity<SliceResponseDto<MemberDto.RESPONSE>> getMembers(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Slice<Member> sliceMembers = memberService.findMembersToSlice(page - 1, size);
        List<MemberDto.RESPONSE> responses = mapper.membersToMemberResponseDtos(sliceMembers.getContent());
        return ResponseEntity.ok(new SliceResponseDto<>(responses, sliceMembers.hasNext()));
    }

    /**
     * Cursor: 첫 요청은 cursor 없이 보낸다. 이후에는 직전 응답의 nextCursor를 그대로 보낸다.
     * 내림차순이므로 다음 요청은 memberId < cursor이다. cursor는 페이지 번호나 offset이 아니다.
     * hasNext가 false이면 nextCursor는 null이며 더 요청하지 않는다.
     */
    @GetMapping("/cursor")
    public ResponseEntity<CursorResponseDto<MemberDto.RESPONSE>> getMembersByCursor(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long cursor) {
        Slice<Member> sliceMembers = memberService.findMembersByCursor(cursor, size);
        List<Member> members = sliceMembers.getContent();
        List<MemberDto.RESPONSE> responses = mapper.membersToMemberResponseDtos(members);
        // Slice는 미리 한 건 더 읽어 hasNext를 계산한다. 응답에는 요청한 size까지만 들어 있다.
        Long nextCursor = sliceMembers.hasNext() ? members.get(members.size() - 1).getMemberId() : null;
        return ResponseEntity.ok(new CursorResponseDto<>(responses, sliceMembers.hasNext(), nextCursor));
    }

    /**
     * 기존 PATCH 경로의 의미는 회원 정보 수정이 아니라 고아 객체 제거 실습이다. 요청 본문은 없다.
     * orderId를 주면 해당 주문을, 생략하면 이 회원의 가장 작은 ID 주문을 관계에서 제거한다.
     * 트랜잭션 종료 시 orphanRemoval로 주문 행도 삭제된다. 대상 회원/주문이 없으면 404다.
     */
    @PatchMapping("/{member-id}")
    public ResponseEntity<Void> patchMember(@PathVariable("member-id") @Positive Long memberId,
            @RequestParam(required = false) @Positive Long orderId) {
        memberService.testPatch(memberId, orderId);
        return ResponseEntity.ok().build();
    }
}
