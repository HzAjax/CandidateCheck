package ru.volodin.loader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private int accepted;
    private int processed;
    private int errors;
    private int remaining;
}
