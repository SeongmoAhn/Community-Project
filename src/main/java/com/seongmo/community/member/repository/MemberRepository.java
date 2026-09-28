package com.seongmo.community.member.repository;

import com.seongmo.community.member.Member;

public interface MemberRepository {
    public Member save(Member request);
    public boolean findByEmail(String email);
}
