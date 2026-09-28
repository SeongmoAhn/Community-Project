package com.seongmo.community.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Long memberId;
    private String memberNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
