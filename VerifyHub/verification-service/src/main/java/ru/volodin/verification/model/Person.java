package ru.volodin.verification.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record Person(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Valid Passport passport,
        @Valid Inn inn,
        @Valid DriverLicense driversLicense
) {}
