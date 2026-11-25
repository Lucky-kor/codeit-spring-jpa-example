// [현재 복습 대상 아님] Repository를 호출하던 후속 단계의 비활성 코드다.
// 현재 예제의 실행 경로는 main → 선택한 Runner → EntityManager이며 이 파일을 거치지 않는다.
// 아래 메서드는 각각 저장 / ID 조회 / 이메일 조회를 위임하던 수업 기록으로만 남긴다.
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
//        // 현재 복습에서는 같은 저장 과정을 Runner의 em.persist(member)로 관찰한다.
//        return memberRepository.save(member);
//    }
//
//    public Member findMember(Long id) {
//        // 현재 복습에서는 em.find(Member.class, id)로 ID 조회부터 확인한다.
//        return memberRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//    }
//
//    public Member findMemberByEmail(String email) {
//        // 이메일 조건 조회는 현재 진도 이후이므로 여기서는 구현을 확장하지 않는다.
//        return memberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//    }
//}
