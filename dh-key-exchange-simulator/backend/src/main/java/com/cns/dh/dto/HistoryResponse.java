package com.cns.dh.dto;

import java.time.Instant;

public record HistoryResponse(
        Long id,
        String mode,
        String p,
        String g,
        String alicePublic,
        String bobPublic,
        boolean secretsMatch,
        Instant createdAt
) {}
