package com.seongmo.community.post.controller;

import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.dto.PostUpdateRequest;
import com.seongmo.community.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @GetMapping
    public List<PostResponse> findAll() {
        return postService.findAll();
    }

    @PostMapping
    public PostResponse create(@RequestBody PostCreateRequest request) {
        return postService.create(request);
    }

    @PatchMapping("/{id}")
    public PostResponse update(@PathVariable Long id, @RequestBody PostUpdateRequest request) {
        return postService.update(id, request);
    }
}
