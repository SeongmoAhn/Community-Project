package com.seongmo.community.post.repository;

import com.seongmo.community.post.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class JdbcPostRepository implements PostRepository {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public Post save(Post request) {
        String sql = "INSERT INTO posts (title, content, member_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, request.getTitle());
            pstmt.setString(2, request.getContent());
            pstmt.setLong(3, request.getMemberId());
            pstmt.setObject(4, request.getCreatedAt());
            pstmt.setObject(5, request.getUpdatedAt());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Post.builder()
                            .id(rs.getLong(1))
                            .title(request.getTitle())
                            .content(request.getContent())
                            .memberId(request.getMemberId())
                            .createdAt(request.getCreatedAt())
                            .updatedAt(request.getUpdatedAt())
                            .build();
                }
            }

            throw new RuntimeException("생성된 id를 찾을 수 없습니다.");
        } catch (SQLException e) {
            throw new RuntimeException("게시글 저장 실패", e);
        }
    }
}
