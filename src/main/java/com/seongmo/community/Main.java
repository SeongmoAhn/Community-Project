package com.seongmo.community;

import com.seongmo.community.member.Member;
import com.seongmo.community.member.MemberService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        MemberService memberService = new MemberService();

        Member seongmo = memberService.save("asm0619@naver.com", "123123123", "안성모");
        System.out.println(seongmo.getId() + ", " + seongmo.getEmail() + ", " + seongmo.getNickname());

        Member testman1 = memberService.save("testman1@test.com", "sdfsdfsdf", "테스트맨1");
        System.out.println(testman1.getId() + ", " + testman1.getEmail() + ", " + testman1.getNickname());

        try {
            Member testman2 = memberService.save("testman1@test.com", "098098098", "테스트맨1-1");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}