package oth.ics.wtp.relaybackend.dtos;

import java.time.LocalDateTime;

public record UserDto(
        String username,
        LocalDateTime registeredAt,
        long followerCount,
        long followingCount
) {
}