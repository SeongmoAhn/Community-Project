package com.seongmo.community.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member {
    @Id
    private final Long id;

    private final String email;
    private final String password;
    private final String nickname;
}
