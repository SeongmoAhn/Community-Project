package com.seongmo.community.post;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Post {
    private final Long id;
    private final String title;
    private final String content;
    private final Long memberId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post(Long id, String title, String content, Long memberId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
}
