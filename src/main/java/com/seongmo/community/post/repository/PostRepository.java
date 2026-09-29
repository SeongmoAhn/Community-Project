package com.seongmo.community.post.repository;

import com.seongmo.community.post.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
}
