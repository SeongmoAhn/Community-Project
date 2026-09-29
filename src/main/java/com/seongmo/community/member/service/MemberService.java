package com.seongmo.community.member.service;

import com.seongmo.community.member.dto.MemberSignupRequest;
import com.seongmo.community.member.dto.MemberSignupResponse;
import com.seongmo.community.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberMapper memberMapper;

    public MemberSignupResponse signup(MemberSignupRequest request) {
        if (memberMapper.existsByEmail(request.getEmail())) {
            log.error("회원가입 실패");
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("email", request.getEmail());
        params.put("password", request.getPassword());
        params.put("nickname", request.getNickname());

        memberMapper.save(params);

        Long generatedId = ((Number) params.get("id")).longValue();

        return MemberSignupResponse.builder()
                .id(generatedId)
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();
    }
}
