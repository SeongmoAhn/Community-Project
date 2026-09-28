package com.seongmo.community.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private Long id = 0L;
    private final MemberRepository memberRepository;

    public Member save(String email, String password, String nickname) {
        if (memberRepository.findByEmail(email)) {
            log.error("회원가입 실패");
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member(++id, email, password, nickname);
        Member saved = memberRepository.save(member);

        return saved;
    }
}
