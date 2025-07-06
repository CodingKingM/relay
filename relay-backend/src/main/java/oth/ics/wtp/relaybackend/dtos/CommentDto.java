package oth.ics.wtp.relaybackend.dtos;

import java.time.Instant;

public record CommentDto(
    Long id,
    String content,
    Instant createdAt,
    String username
) {} 