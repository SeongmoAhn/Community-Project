package com.seongmo.community.member;

public class MemberService {
    private Long id = 0L;
    private MemberRepository memberRepository = new MemoryMemberRepository();

    public Member save(String email, String password, String nickname) {
        if (memberRepository.findByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member(++id, email, password, nickname);
        Member saved = memberRepository.save(member);
        return saved;
    }
}
