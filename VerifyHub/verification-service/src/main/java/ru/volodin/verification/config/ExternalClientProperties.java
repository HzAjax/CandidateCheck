package ru.volodin.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.external")
@Getter
@Setter
public class ExternalClientProperties {
    private String baseUrl;
    private String passportPath;
    private String innPath;
    private String dlPath;
}
