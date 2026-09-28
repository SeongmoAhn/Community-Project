package com.seongmo.community.post.controller;

import com.seongmo.community.post.Post;
import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public PostResponse create(@RequestBody PostCreateRequest request) {
        return postService.create(request);
    }
}
