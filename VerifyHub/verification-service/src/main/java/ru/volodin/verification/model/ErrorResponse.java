package ru.volodin.verification.model;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        String traceId,
        Instant timestamp,
        String path
) {}
