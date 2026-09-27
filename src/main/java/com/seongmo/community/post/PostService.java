package com.seongmo.community.post;

import java.time.LocalDateTime;

public class PostService {
    private PostRepository postRepository = new MemoryPostRepository();
    private Long id = 0L;

    public Post save(String title, String content, Long memberId) {
        Post request = new Post(++id, title, content, memberId, LocalDateTime.now());
        Post saved = postRepository.save(request);
        return saved;
    }
}
