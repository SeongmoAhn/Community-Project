package com.seongmo.community.post;

import java.util.HashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryPostRepository implements PostRepository {
    private final HashMap<Long, Post> hashMap = new HashMap<>();

    @Override
    public Post save(Post request) {
        hashMap.put(request.getId(), request);
        Post saved = hashMap.get(request.getId());
        return saved;
    }
}
