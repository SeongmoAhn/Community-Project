package com.seongmo.community.post.controller;

import com.seongmo.community.post.dto.PostCreateRequest;
import com.seongmo.community.post.dto.PostPageResponse;
import com.seongmo.community.post.dto.PostResponse;
import com.seongmo.community.post.dto.PostUpdateRequest;
import com.seongmo.community.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @GetMapping
    public PostPageResponse findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.findAll(keyword, memberId, cursor, size);
    }

    @PostMapping
    public PostResponse create(@RequestBody PostCreateRequest request) {
        return postService.create(request);
    }

    @PatchMapping("/{id}")
    public PostResponse update(@PathVariable Long id, @RequestBody PostUpdateRequest request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }
}
