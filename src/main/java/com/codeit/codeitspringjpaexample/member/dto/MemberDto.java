package com.codeit.codeitspringjpaexample.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 요청 필드와 응답 필드를 분리한다. 관계 엔티티나 변경하면 안 되는 ID를 POST로 받지 않는다. */
public class MemberDto {
    // Jackson은 기본 생성자로 객체를 만들고 setter로 JSON 값을 채운다. @Valid가 아래 제약을 검사한다.
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class POST {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String name;
        @NotBlank
        @Pattern(regexp = "\\d{3}-\\d{4}-\\d{4}", message = "전화번호는 010-1234-5678 형식이어야 합니다.")
        private String phone;
    }

    // MapStruct가 생성자를 이용해 만든다. memberId는 엔티티/커서와 같은 Long 타입으로 반환한다.
    @AllArgsConstructor
    @Getter
    public static class RESPONSE {
        private Long memberId;
        private String email;
        private String name;
        private String phone;
        private String createdAt;
        private String modifiedAt;
    }
}
