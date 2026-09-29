package com.seongmo.community.post.service;

import com.seongmo.community.member.repository.MemberRepository;
import com.seongmo.community.post.Post;
import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostResponse create(PostCreateRequest request) {
        String memberNickname = memberRepository.findNicknameById(request.getMemberId());
        if (memberNickname == null) {
            throw new IllegalArgumentException("존재하지 않는 memberId입니다.");
        }

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
                .memberNickname(memberNickname)
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
