package com.seongmo.community.member.mapper;

import com.seongmo.community.member.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.Optional;

@Mapper
public interface MemberMapper {

    @Insert("INSERT INTO members (email, password, nickname) VALUES (#{email}, #{password}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Map<String, Object> params);

    @Select("SELECT COUNT(*) > 0 FROM members WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT * FROM members WHERE id = #{id}")
    Optional<Member> findById(Long id);
}
