package com.seongmo.community.post;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Builder
@Table(name = "posts")
public class Post {
    @Id
    private final Long id;

    private final String title;
    private final String content;
    private final Long memberId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
