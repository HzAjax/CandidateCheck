package ru.volodin.loader.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DriverLicense {

    private String series;
    private String number;
    private LocalDate dateIssue;
}
