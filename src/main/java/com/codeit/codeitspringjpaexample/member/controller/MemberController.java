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
//    public ResponseEntity postMember(@RequestBody MemberPostDto dto) {
//        Member member = new Member();
//        member.setEmail(dto.getEmail());
//        member.setName(dto.getName());
//        return ResponseEntity.created(null).body(memberService.createMember(member));
//    }
//
//    @GetMapping("/{email}")
//    public ResponseEntity getMember(@PathVariable("email") String email) {
//        return ResponseEntity.ok(memberService.findMemberByEmail(email));
//    }
//}
