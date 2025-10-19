package ru.volodin.verification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.volodin.verification.client.ExternalCheckClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ExternalClientProperties.class)
@RequiredArgsConstructor
public class HttpClientConfig {

    private final ConfigurableBeanFactory beanFactory;

    @Bean
    public ExternalCheckClient externalCheckClient(ExternalClientProperties props) {

        HttpClient jdkClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkClient);

        RestClient restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .embeddedValueResolver(beanFactory::resolveEmbeddedValue)
                .build();

        return factory.createClient(ExternalCheckClient.class);
    }
}
