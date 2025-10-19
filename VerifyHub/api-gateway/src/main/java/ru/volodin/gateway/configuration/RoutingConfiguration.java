package ru.volodin.gateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.volodin.gateway.configuration.props.RemoteServiceProperties;

@Configuration
@EnableConfigurationProperties(RemoteServiceProperties.class)
@RequiredArgsConstructor
public class RoutingConfiguration {

    private final RemoteServiceProperties props;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        String lbUri = "lb://" + props.getVerificationServiceId();

        return builder.routes()
                .route("verification", r -> r
                        .path("/api/v1/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(lbUri))
                .build();
    }
}
