package com.seongmo.community.post.service;

import com.seongmo.community.member.Member;
import com.seongmo.community.member.mapper.MemberMapper;
import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostMapper postMapper;
    private final MemberMapper memberMapper;

    public PostResponse create(PostCreateRequest request) {
        Member member = memberMapper.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId입니다."));

        Map<String, Object> params = new HashMap<>();
        params.put("title", request.getTitle());
        params.put("content", request.getContent());
        params.put("memberId", request.getMemberId());
        params.put("createdAt", LocalDateTime.now());
        params.put("updatedAt", LocalDateTime.now());

        postMapper.save(params);

        Long generatedId = ((Number) params.get("id")).longValue();

        return PostResponse.builder()
                .id(generatedId)
                .title(request.getTitle())
                .content(request.getContent())
                .memberId(request.getMemberId())
                .memberNickname(member.getNickname())
                .createdAt((LocalDateTime) params.get("createdAt"))
                .updatedAt((LocalDateTime) params.get("updatedAt"))
                .build();
    }
}
