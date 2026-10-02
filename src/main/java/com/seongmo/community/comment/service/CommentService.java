package com.seongmo.community.comment.service;

import com.seongmo.community.comment.Comment;
import com.seongmo.community.comment.dto.CommentRequest;
import com.seongmo.community.comment.dto.CommentResponse;
import com.seongmo.community.comment.repository.CommentRepository;
import com.seongmo.community.member.Member;
import com.seongmo.community.member.repository.MemberRepository;
import com.seongmo.community.post.Post;
import com.seongmo.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public CommentResponse create(Long postId, CommentRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId입니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 postId입니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .member(member)
                .post(post)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentResponse.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .memberId(saved.getMember().getId())
                .postId(saved.getPost().getId())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public CommentResponse update(Long id, CommentRequest request) {
        Comment updated = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 commentId입니다."));

        if (!updated.getMember().getId().equals(request.getMemberId())) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다.");
        }

        updated.setContent(request.getContent());
        updated.setUpdatedAt(LocalDateTime.now());

        return CommentResponse.builder()
                .id(updated.getId())
                .content(updated.getContent())
                .memberId(updated.getMember().getId())
                .postId(updated.getPost().getId())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }
}
