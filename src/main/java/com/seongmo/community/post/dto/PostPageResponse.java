package com.seongmo.community.post.dto;

import java.util.List;

public record PostPageResponse(List<PostResponse> items, String nextCursor, boolean hasNext) {
}
