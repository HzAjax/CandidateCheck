package ru.volodin.verification.model;

public record CheckResponse(
        String status,
        TypeCheck typeCheck,
        String traceId
) {}
