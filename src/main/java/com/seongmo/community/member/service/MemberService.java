package com.seongmo.community.member.service;

import com.seongmo.community.member.Member;
import com.seongmo.community.member.dto.MemberSignupRequest;
import com.seongmo.community.member.dto.MemberSignupResponse;
import com.seongmo.community.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private Long id = 0L;
    private final MemberRepository memberRepository;

    public MemberSignupResponse signup(MemberSignupRequest request) {
        if (memberRepository.findByEmail(request.getEmail())) {
            log.error("회원가입 실패");
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.builder()
                .id(++id)
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .build();

        Member saved = memberRepository.save(member);

        MemberSignupResponse response = MemberSignupResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .nickname(saved.getNickname())
                .build();

        return response;
    }
}
