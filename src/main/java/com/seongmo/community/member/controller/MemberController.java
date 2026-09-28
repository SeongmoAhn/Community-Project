package com.seongmo.community.member.controller;

import com.seongmo.community.member.dto.MemberSignupRequest;
import com.seongmo.community.member.dto.MemberSignupResponse;
import com.seongmo.community.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public MemberSignupResponse signup(@RequestBody MemberSignupRequest request) {
        return memberService.save(request);
    }
}
