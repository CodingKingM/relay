package oth.ics.wtp.relaybackend.dtos;

import java.time.LocalDateTime;
import java.time.Instant;

public record UserDto(
        String username,
        Instant registeredAt,
        int followerCount,
        int followingCount,
        String fullName,
        String email,
        String biography
) {}