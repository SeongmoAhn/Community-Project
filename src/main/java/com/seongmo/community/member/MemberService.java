package com.seongmo.community.member;

import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private Long id = 0L;
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member save(String email, String password, String nickname) {
        if (memberRepository.findByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member(++id, email, password, nickname);
        Member saved = memberRepository.save(member);
        return saved;
    }
}
