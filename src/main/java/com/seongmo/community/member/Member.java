package com.seongmo.community.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class Member {
    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
}
