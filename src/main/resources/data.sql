-- PostgreSQL 전용 수업 기록. 기본 실행은 이 파일 대신 MemberDataRunner가 데이터를 준비한다.
-- 수동 사용하려면 dev 프로필에서 --spring.sql.init.mode=always 를 명시한다.
-- H2에서는 ON CONFLICT / setval을 사용할 수 없으므로 실행하지 않는다.
-- 회원 ID 1~10, 주문 ID 1~5와 FK 연결, IDENTITY 시퀀스 보정을 관찰하는 예제다.
INSERT INTO members (
    member_id,
    email,
    name,
    phone,
    created_at,
    modified_at
)
VALUES
    (1, 'member1@example.com', '김철수', '010-0000-0001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'member2@example.com', '이영희', '010-0000-0002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'member3@example.com', '박지민', '010-0000-0003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'member4@example.com', '최유나', '010-0000-0004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'member5@example.com', '정우성', '010-0000-0005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'member6@example.com', '한지민', '010-0000-0006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'member7@example.com', '오세훈', '010-0000-0007', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'member8@example.com', '서지혜', '010-0000-0008', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'member9@example.com', '문지훈', '010-0000-0009', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'member10@example.com', '윤가영', '010-0000-0010', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (member_id) DO NOTHING;

-- ON CONFLICT: 해당 ID가 이미 있으면 추가하지 않습니다.
-- 앱을 다시 실행해도 같은 ID의 데이터가 중복되지 않습니다.


-- 2. 주문 5건
-- EnumType.STRING이므로 enum 상수 이름을 저장합니다.
INSERT INTO orders (
    order_id,
    member_id,
    order_status,
    created_at,
    modified_at
)
VALUES
    -- 1번 회원의 주문 3건
    (1, 1, 'ORDER_REQUEST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'ORDER_CONFIRM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 'ORDER_COMPLETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 2번 회원의 주문 2건
    (4, 2, 'ORDER_REQUEST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 2, 'ORDER_CANCEL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (order_id) DO NOTHING;


-- 3. 자동 증가 번호 보정
-- ID를 직접 입력하면 자동 증가 시퀀스는 함께 증가하지 않습니다.
-- 이후 JPA로 저장할 때 기존 ID와 충돌하지 않도록 보정합니다.
SELECT setval(
               pg_get_serial_sequence('members', 'member_id'),
               (SELECT MAX(member_id) FROM members),
               true
       );

SELECT setval(
               pg_get_serial_sequence('orders', 'order_id'),
               (SELECT MAX(order_id) FROM orders),
               true
       );
