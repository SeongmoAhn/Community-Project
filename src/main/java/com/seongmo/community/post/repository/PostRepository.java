package com.seongmo.community.post.repository;

import com.seongmo.community.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
