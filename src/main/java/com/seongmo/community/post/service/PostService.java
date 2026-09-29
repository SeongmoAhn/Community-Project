package com.seongmo.community.post.service;

import com.seongmo.community.member.Member;
import com.seongmo.community.member.repository.MemberRepository;
import com.seongmo.community.post.Post;
import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.dto.PostUpdateRequest;
import com.seongmo.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostResponse create(PostCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId입니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .memberId(request.getMemberId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post saved = postRepository.save(post);

        return PostResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .memberId(saved.getMemberId())
                .memberNickname(member.getNickname())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 postId입니다."));

        boolean changed = false;
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
            changed = true;
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
            changed = true;
        }
        if (changed) {
            post.setUpdatedAt(LocalDateTime.now());
        }

        Member member = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId입니다."));

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .memberId(post.getMemberId())
                .memberNickname(member.getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
