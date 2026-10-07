package com.seongmo.community.post.repository;

import com.seongmo.community.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 첫 페이지, 커서 없음
    @Query("SELECT p FROM Post p JOIN FETCH p.member ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findFirstPage(Pageable pageable);

    // 다음 페이지, 마지막으로 본 (createdAt, id) 이후
    @Query("SELECT p FROM Post p JOIN FETCH p.member " +
            "WHERE p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findNextPage(@Param("createdAt")LocalDateTime createdAt,
                            @Param("id") Long id,
                            Pageable pageable);
}
