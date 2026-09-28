package com.seongmo.community.post;

import com.seongmo.community.member.MemberRepository;
import com.seongmo.community.member.MemoryMemberRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
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
