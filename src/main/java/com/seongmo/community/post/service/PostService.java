package com.seongmo.community.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private Long id = 0L;
    private final PostRepository postRepository;

    public Post save(String title, String content, Long memberId) {

        Post request = new Post(++id, title, content, memberId, LocalDateTime.now());
        Post saved = postRepository.save(request);

        return saved;
    }
}
