//package com.codeit.codeitspringjpaexample.member.service;
//
//import com.codeit.codeitspringjpaexample.member.entity.Member;
//import com.codeit.codeitspringjpaexample.member.repository.MemberRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@AllArgsConstructor
//public class MemberService {
//    private final MemberRepository memberRepository;
//
//    public Member createMember(Member member) {
//        return memberRepository.save(member);
//    }
//
//    public Member findMember(Long id) {
//        return memberRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//    }
//
//    public Member findMemberByEmail(String email) {
//        return memberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//    }
//}
