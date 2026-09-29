package com.seongmo.community.member.repository;

import com.seongmo.community.member.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, Long> {
    boolean existsByEmail(String email);
}
