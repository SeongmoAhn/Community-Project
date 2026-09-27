package com.seongmo.community;

import com.seongmo.community.member.MemberRepository;
import com.seongmo.community.member.MemberService;
import com.seongmo.community.member.MemoryMemberRepository;
import com.seongmo.community.post.MemoryPostRepository;
import com.seongmo.community.post.PostRepository;
import com.seongmo.community.post.PostService;

public class AppConfig {
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final PostService postService;

    public AppConfig() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
        postRepository = new MemoryPostRepository();
        postService = new PostService(postRepository);
    }

    public MemberRepository memberRepository() {
        return memberRepository;
    }

    public MemberService memberService() {
        return memberService;
    }

    public PostRepository postRepository() {
        return postRepository;
    }

    public PostService postService() {
        return postService;
    }
}
