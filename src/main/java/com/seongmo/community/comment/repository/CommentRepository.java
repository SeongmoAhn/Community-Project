package com.seongmo.community.comment.repository;

import com.seongmo.community.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteByPostId(Long postId);
}
