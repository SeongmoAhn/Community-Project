package com.seongmo.community.comment.controller;

import com.seongmo.community.comment.dto.CommentRequest;
import com.seongmo.community.comment.dto.CommentResponse;
import com.seongmo.community.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public CommentResponse create(@PathVariable Long postId, @RequestBody CommentRequest request) {
        return commentService.create(postId, request);
    }

    @PatchMapping("/comments/{id}")
    public CommentResponse update(@PathVariable Long id, @RequestBody CommentRequest request) {
        return commentService.update(id, request);
    }
}
