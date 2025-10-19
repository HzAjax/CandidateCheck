package ru.volodin.verification.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckRequest(
        @NotBlank String requestId,
        @NotNull TypeCheck typeCheck,
        @NotNull  @Valid Person person
) {}