package oth.ics.wtp.relaybackend.dtos;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String content,
        String authorUsername,
        LocalDateTime createdAt,
        int likeCount,
        boolean isLikedByCurrentUser
) {
}