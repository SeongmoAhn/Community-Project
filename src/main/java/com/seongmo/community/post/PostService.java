package com.seongmo.community.post;

import java.time.LocalDateTime;

public class PostService {
    private Long id = 0L;
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post save(String title, String content, Long memberId) {
        Post request = new Post(++id, title, content, memberId, LocalDateTime.now());
        Post saved = postRepository.save(request);
        return saved;
    }
}
