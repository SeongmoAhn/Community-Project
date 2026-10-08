package com.seongmo.community.post.service;

import com.seongmo.community.comment.repository.CommentRepository;
import com.seongmo.community.member.Member;
import com.seongmo.community.member.repository.MemberRepository;
import com.seongmo.community.post.Post;
import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostPageResponse;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.dto.PostUpdateRequest;
import com.seongmo.community.post.exception.InvalidCursorException;
import com.seongmo.community.post.repository.PostQueryRepository;
import com.seongmo.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    private record PageStats(LocalDateTime createdAt, Long id) {}
    private static final int MAX_SIZE = 50;

    public PostResponse create(PostCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId입니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post saved = postRepository.save(post);

        return PostResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .memberId(member.getId())
                .memberNickname(member.getNickname())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 postId입니다."));

        boolean changed = false;
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
            changed = true;
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
            changed = true;
        }
        if (changed) {
            post.setUpdatedAt(LocalDateTime.now());
        }

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .memberId(post.getMember().getId())
                .memberNickname(post.getMember().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // "createdAt_id" 형태로 반환
    private String encodeCursor(Post post) {
        return post.getCreatedAt().toString() + "_" + post.getId();
    }

    // cursor를 {createdAt, id}로 디코딩 후 반환
    private PageStats decodeCursor(String cursor) {
        try {
            String[] parts = cursor.split("_", 2);
            return new PageStats(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidCursorException("잘못된 cursor 형식입니다.");
        }
    }

    public PostPageResponse findAll(String keyword, Long memberId, String cursor, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);

        PageStats stats = (cursor == null || cursor.isBlank()) ? null : decodeCursor(cursor);
        List<Post> posts = postQueryRepository.findPage(
                keyword,
                memberId,
                stats == null ? null : stats.createdAt,
                stats == null ? null : stats.id,
                pageSize + 1);

        boolean hasNext = posts.size() > pageSize;
        List<Post> page = hasNext ? posts.subList(0, pageSize) : posts;
        String nextCursor = hasNext ? encodeCursor(page.get(page.size() - 1)) : null;

        List<PostResponse> items = page.stream().map(post ->
                PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .memberId(post.getMember().getId())
                        .memberNickname(post.getMember().getNickname())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .build()).toList();

        return new PostPageResponse(items, nextCursor, hasNext);
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID입니다."));

        commentRepository.deleteByPostId(post.getId());

        postRepository.delete(post);
    }
}
