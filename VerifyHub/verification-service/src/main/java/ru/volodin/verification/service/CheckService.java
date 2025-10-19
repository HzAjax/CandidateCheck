package ru.volodin.verification.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.volodin.verification.client.ExternalCheckClient;
import ru.volodin.verification.model.CheckRequest;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckService {
    private static final Logger log = LoggerFactory.getLogger(CheckService.class);
    private final ExternalCheckClient client;

    /**
     * Обрабатывает входящий запрос проверки:
     * 1) валидирует, что запрос не null,
     * 2) формирует payload для внешнего сервиса,
     * 3) перенаправляет в нужный метод клиента по типу проверки,
     * 4) пишет краткий лог об отправке.
     *
     * @param req входной запрос проверки (requestId, typeCheck, person)
     * @throws IllegalArgumentException если req == null или тип не поддержан
     */
    public void process(CheckRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("CheckRequest must not be null");
        }

        Map<String, Object> payload = Map.of(
                "requestId", req.requestId(),
                "person", req.person()
        );

        switch (req.typeCheck()) {
            case PASSPORT -> {
                client.sendPassport(payload);
                log.info("passport was sent");
            }
            case INN -> {
                client.sendInn(payload);
                log.info("inn was sent");
            }
            case DRIVERS_LICENSE -> {
                client.sendDl(payload);
                log.info("driver's license was sent");
            }
            default -> throw new IllegalArgumentException("Unsupported typeCheck: " + req.typeCheck());
        }
    }
}
