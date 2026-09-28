package com.seongmo.community.member;

import java.util.HashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryMemberRepository implements MemberRepository{
    private final HashMap<Long, Member> hashMap = new HashMap<>();

    @Override
    public Member save(Member request) {
        hashMap.put(request.getId(), request);
        return hashMap.get(request.getId());
    }

    @Override
    public boolean findByEmail(String email) {
        for (Member m : hashMap.values()) {
            if (m.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }
}
