package com.seongmo.community.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {
    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
}
