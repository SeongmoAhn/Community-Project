package com.seongmo.community.post.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.Map;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO posts (title, content, member_id, created_at, updated_at) VALUES (#{title}, #{content}, #{memberId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Map<String, Object> params);
}
