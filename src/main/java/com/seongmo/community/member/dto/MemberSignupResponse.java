package com.seongmo.community.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSignupResponse {
    private Long id;
    private String email;
    private String nickname;
}
