package com.codeit.codeitspringjpaexample.member.service;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member createMember(Member member) {
        // 중복된 이메일, 전화번호 검증
        verifyExistsEmail(member.getEmail());
        verifyExistsPhone(member.getPhone());
        // 이 아래는 이미 중복이 걸러진 상태
        return memberRepository.save(member);
    }

    private void verifyExistsEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if(findMember.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }
    private void verifyExistsPhone(String phone) {
        Optional<Member> findMember = memberRepository.findByPhone(phone);
        if(findMember.isPresent()) {
            throw new RuntimeException("Phone already exists");
        }
    }
}
