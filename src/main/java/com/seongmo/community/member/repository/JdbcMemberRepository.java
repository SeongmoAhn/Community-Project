package com.seongmo.community.member.repository;

import com.seongmo.community.member.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public Member save(Member request) {
        String sql = "INSERT INTO members (email, password, nickname) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, request.getEmail());
            pstmt.setString(2, request.getPassword());
            pstmt.setString(3, request.getNickname());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Member.builder()
                            .id(rs.getLong(1))
                            .email(request.getEmail())
                            .password(request.getPassword())
                            .nickname(request.getNickname())
                            .build();
                }
            }

            throw new RuntimeException("생성된 id를 찾을 수 없습니다.");
        } catch (SQLException e) {
            throw new RuntimeException("회원 저장 실패", e);
        }
    }

    @Override
    public boolean findByEmail(String email) {
        String sql = "SELECT id FROM members WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("이메일 조회 실패", e);
        }
    }

    @Override
    public String findNicknameById(Long id) {
        String sql = "SELECT nickname FROM members WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ID 조회 실패", e);
        }
    }
}
