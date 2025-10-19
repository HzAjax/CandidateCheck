package ru.volodin.verification.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

@HttpExchange
public interface ExternalCheckClient {

    @PostExchange("${client.external.passport-path}")
    Map<String, Object> sendPassport(@RequestBody Map<String, Object> body);

    @PostExchange("${client.external.inn-path}")
    Map<String, Object> sendInn(@RequestBody Map<String, Object> body);

    @PostExchange("${client.external.dl-path}")
    Map<String, Object> sendDl(@RequestBody Map<String, Object> body);
}
