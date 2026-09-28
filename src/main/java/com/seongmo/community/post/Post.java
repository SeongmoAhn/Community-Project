package com.seongmo.community.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Post {
    private final Long id;
    private final String title;
    private final String content;
    private final Long memberId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
