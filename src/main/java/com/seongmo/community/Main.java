package com.seongmo.community;

import com.seongmo.community.member.Member;
import com.seongmo.community.post.Post;

public class Main {
    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();

        Member seongmo = appConfig.memberService().save("asm0619@naver.com", "123123123", "안성모");
        System.out.println(seongmo.getId() + ", " + seongmo.getEmail() + ", " + seongmo.getNickname());

        Member testman1 = appConfig.memberService().save("testman1@test.com", "sdfsdfsdf", "테스트맨1");
        System.out.println(testman1.getId() + ", " + testman1.getEmail() + ", " + testman1.getNickname());

        try {
            Member testman2 = appConfig.memberService().save("testman1@test.com", "098098098", "테스트맨1-1");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        Post post1 = appConfig.postService().save("성모 제목", "성모 본문", seongmo.getId());
        System.out.println(post1.getId() + ", " + post1.getTitle() + ", " + post1.getContent() + ", " + post1.getMemberId() + ", " + post1.getCreatedAt());

        Post post2 = appConfig.postService().save("테스트맨1 제목", "테스트맨1 본문", testman1.getId());
        System.out.println(post2.getId() + ", " + post2.getTitle() + ", " + post2.getContent() + ", " + post2.getMemberId() + ", " + post2.getCreatedAt());
    }
}