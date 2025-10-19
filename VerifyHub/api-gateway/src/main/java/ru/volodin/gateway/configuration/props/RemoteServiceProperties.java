package ru.volodin.gateway.configuration.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.remote")
@Getter @Setter
public class RemoteServiceProperties {
    private String verificationServiceId = "verification-service";
}
