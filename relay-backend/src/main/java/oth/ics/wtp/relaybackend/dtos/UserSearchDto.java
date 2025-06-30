package oth.ics.wtp.relaybackend.dtos;

import java.time.LocalDateTime;

public record UserSearchDto(
        String username,
        LocalDateTime registeredAt,
        boolean isFollowing
) {
}