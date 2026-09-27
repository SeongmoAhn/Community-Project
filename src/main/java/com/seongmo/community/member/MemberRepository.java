package com.seongmo.community.member;

public interface MemberRepository {
    public Member save(Member request);
    public boolean findByEmail(String email);
}
