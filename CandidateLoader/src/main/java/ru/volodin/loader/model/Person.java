package ru.volodin.loader.model;

import lombok.Getter;

@Getter
public class Person {
    private String firstName;
    private String lastName;
    private TypeCheck typeCheck;
    private Passport passport;
    private Inn inn;
    private DriverLicense driversLicense;
}
