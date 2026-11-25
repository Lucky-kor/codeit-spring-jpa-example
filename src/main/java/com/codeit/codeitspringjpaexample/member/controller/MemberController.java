// [현재 복습 대상 아님] 기존 수업 기록을 남긴 비활성 HTTP 예제다.
// 현재 진도에서는 EntityManager로 직접 실행한다. 이 파일은 주석 해제하지 않는다.
// 아래 흐름은 요청 수신 → DTO 값을 엔티티에 복사 → Service 호출 → 응답 반환이다.
// 필수 phone 값이 빠진 이전 단계 코드라서 전체 주석 해제만으로는 실행할 수 없다.
//package com.codeit.codeitspringjpaexample.member.controller;
//
//import com.codeit.codeitspringjpaexample.member.dto.MemberPostDto;
//import com.codeit.codeitspringjpaexample.member.entity.Member;
//import com.codeit.codeitspringjpaexample.member.service.MemberService;
//import lombok.AllArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/v11/members")
//@RequiredArgsConstructor
//public class MemberController {
//    private final MemberService memberService;
//
//    @PostMapping
//    // 요청값을 일반 DTO로 받은 다음 Member 객체로 옮기던 부분이다.
//    public ResponseEntity postMember(@RequestBody MemberPostDto dto) {
//        Member member = new Member();
//        member.setEmail(dto.getEmail());
//        member.setName(dto.getName());
//        return ResponseEntity.created(null).body(memberService.createMember(member));
//    }
//
//    @GetMapping("/{email}")
//    // 후속 진도에서 Service와 연결할 예제 흔적이다. 현재 학습 경로에는 포함하지 않는다.
//    public ResponseEntity getMember(@PathVariable("email") String email) {
//        return ResponseEntity.ok(memberService.findMemberByEmail(email));
//    }
//}
