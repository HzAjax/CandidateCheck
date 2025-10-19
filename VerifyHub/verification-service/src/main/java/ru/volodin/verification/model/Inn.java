package ru.volodin.verification.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Inn(
        @NotBlank(message = "Inn series is required")
        @Pattern(regexp = "\\d{4}", message = "Inn series must be 4 digits")
        String series,

        @NotBlank(message = "Inn number is required")
        @Pattern(regexp = "\\d{9}", message = "Inn number must be 9 digits")
        String number,

        @NotBlank(message = "Inn is required")
        @Pattern(regexp = "\\d{12}", message = "Inn must be 12 digits")
        String inn
) {}
