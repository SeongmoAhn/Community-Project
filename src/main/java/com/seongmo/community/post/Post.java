package com.seongmo.community.post;

import java.time.LocalDateTime;

public class Post {
    private Long id;
    private String title;
    private String content;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post(Long id, String title, String content, Long memberId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public Long getId() {
        return id;
    }
}
