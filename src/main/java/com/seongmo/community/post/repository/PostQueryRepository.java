package com.seongmo.community.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongmo.community.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.seongmo.community.member.QMember.member;
import static com.seongmo.community.post.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<Post> findPage(String keyword, Long memberId, LocalDateTime cursorCreatedAt, Long cursorId, int limit) {
        return queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .where(titleContains(keyword),
                        memberIdEq(memberId),
                        cursorCondition(cursorCreatedAt, cursorId))
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    // 조건이 없으면 null -> where()가 무시한다
    private BooleanExpression titleContains(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : post.title.contains(keyword);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId == null ? null : post.member.id.eq(memberId);
    }

    private BooleanExpression cursorCondition(LocalDateTime createdAt, Long id) {
        if (createdAt == null || id == null) {
            return null;
        }
        return post.createdAt.lt(createdAt)
                .or(post.createdAt.eq(createdAt).and(post.id.lt(id)));
    }
}
