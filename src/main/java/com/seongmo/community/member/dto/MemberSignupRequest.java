package com.seongmo.community.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MemberSignupRequest {
    private String email;
    private String password;
    private String nickname;
}
