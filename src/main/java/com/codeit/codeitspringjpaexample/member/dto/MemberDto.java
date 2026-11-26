package com.codeit.codeitspringjpaexample.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MemberDto {
    @AllArgsConstructor
    @Getter
    public static class POST {
        private String email;
        private String name;
        private String phone;
    }

    @AllArgsConstructor
    @Getter
    public static class RESPONSE {
        private String memberId;
        private String email;
        private String name;
        private String phone;
        private String createdAt;
        private String modifiedAt;
    }
}
