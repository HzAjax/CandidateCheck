package ru.volodin.verification.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record Passport(
        @NotBlank(message = "Passport series is required")
        @Pattern(regexp = "\\d{4}", message = "Passport series must be 4 digits")
        String series,

        @NotBlank(message = "Passport number is required")
        @Pattern(regexp = "\\d{6}", message = "Passport number must be 6 digits")
        String number,

        @NotNull(message = "DateIssue is required")
        @PastOrPresent(message = "DateIssue must not be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateIssue
) {}
