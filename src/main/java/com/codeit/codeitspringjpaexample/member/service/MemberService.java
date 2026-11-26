package com.codeit.codeitspringjpaexample.member.service;

import com.codeit.codeitspringjpaexample.member.entity.Member;
import com.codeit.codeitspringjpaexample.member.entity.MemberStatus;
import com.codeit.codeitspringjpaexample.member.repository.MemberRepository;
import com.codeit.codeitspringjpaexample.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

/**
 * [API 실습 2] 요청의 저장/조회 절차를 조합한다. 조회문 자체는 MemberRepository에서 확인한다.
 * @Transactional 범위 안에서 엔티티를 관리하므로 변경 감지, cascade, orphanRemoval을 관찰할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    /** 새 회원의 상태는 따로 save하지 않는다. Member.memberStatus의 cascade PERSIST로 함께 저장한다. */
    @Transactional
    public Member createMember(Member member) {
        verifyExistsEmail(member.getEmail());
        verifyExistsPhone(member.getPhone());
        member.setMemberStatus(new MemberStatus()); // 편의 메서드가 양쪽의 참조를 함께 연결한다.
        try {
            // flush로 실제 INSERT와 DB 제약을 여기서 확인한다. flush 자체는 commit이 아니다.
            return memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            // 사전 조회 직후 다른 요청이 같은 값을 저장하는 경우도 DB의 unique 제약이 막는다.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일 또는 전화번호입니다.", e);
        }
    }

    /** Page는 전체 건수도 계산할 수 있다. 결과가 적으면 Spring Data가 count를 생략하기도 한다. */
    @Transactional(readOnly = true)
    public Page<Member> findMembers(int page, int size) {
        return memberRepository.findAll(PageRequest.of(page, size, Sort.by("memberId").descending()));
    }

    /**
     * 메서드의 반환형만 Slice로 바꾸는 것으로는 충분하지 않다.
     * findAll(Pageable)은 Page를 만들므로, Slice를 반환하는 파생 쿼리를 실제로 호출한다.
     */
    @Transactional(readOnly = true)
    public Slice<Member> findMembersToSlice(int page, int size) {
        return memberRepository.findAllByOrderByMemberIdDesc(PageRequest.of(page, size));
    }

    /**
     * 커서도 Slice의 size + 1 조회를 사용하므로 전체 count가 없다.
     * 항상 0번 묶음을 요청하되, where memberId < cursor 조건으로 조회 시작 위치를 바꾼다.
     */
    @Transactional(readOnly = true)
    public Slice<Member> findMembersByCursor(Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return cursor == null
                ? memberRepository.findAllByOrderByMemberIdDesc(pageable)
                : memberRepository.findAllByMemberIdLessThanOrderByMemberIdDesc(cursor, pageable);
    }

    /**
     * 같은 데이터를 세 방식으로 읽은 뒤 orders.size를 실제로 사용해 지연 로딩 SQL을 관찰한다.
     * basic에는 상태의 EAGER 조회도 더해질 수 있어 SQL 수를 무조건 '회원 수 + 1'로 단정하지 않는다.
     * fetch/graph는 orders와 memberStatus를 함께 가져온다. 이 비교에는 페이지 크기를 적용하지 않는다.
     */
    @Transactional(readOnly = true)
    public List<Member> findTestMembers(String mode) {
        System.out.println("[N+1 시작] mode=" + mode + " : 이후 SELECT와 orders 접근 시점을 비교하세요.");
        List<Member> members = switch (mode) {
            case "basic" -> memberRepository.findAll(Sort.by("memberId").descending());
            case "fetch" -> memberRepository.findAllWithOrders();
            case "graph" -> memberRepository.findAllWithOrdersEntityGraph();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode는 basic, fetch, graph 중 하나입니다.");
        };
        for (Member member : members) {
            System.out.println("[N+1 " + mode + "] 회원=" + member.getMemberId()
                    + ", 주문 수=" + member.getOrders().size());
        }
        System.out.println("[N+1 종료] mode=" + mode + ", 회원 수=" + members.size());
        return members;
    }

    /**
     * 고정 인덱스 get(1) 대신 존재하는 주문을 선택한다. 양쪽 참조를 해제하고 orphanRemoval을 확인한다.
     * orderId가 있으면 그 주문만 대상으로 삼아 다른 회원의 주문을 제거하지 않는다.
     */
    @Transactional
    public void testPatch(Long memberId, Long orderId) {
        Member member = findMemberOrThrow(memberId);
        Order order = member.getOrders().stream()
                .filter(candidate -> orderId == null || orderId.equals(candidate.getOrderId()))
                .min(Comparator.comparing(Order::getOrderId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제거할 회원의 주문이 없습니다."));
        member.removeOrder(order);
        // 별도 orderRepository.delete 없이, 컬렉션에서 빠진 주문에 DELETE가 나가는지 관찰한다.
        memberRepository.flush();
    }

    /** 상태와 주문은 회원의 수명에 따라 삭제한다. 주문항목도 함께 삭제되지만 커피는 별도 메뉴라 남는다. */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        try {
            memberRepository.delete(member);
            memberRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "연관 데이터 때문에 회원을 삭제할 수 없습니다.", e);
        }
    }

    // Optional이 비어 있으면 HTTP 응답에서도 찾을 수 없음을 알린다.
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    private void verifyExistsEmail(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }

    private void verifyExistsPhone(String phone) {
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다.");
        }
    }
}
