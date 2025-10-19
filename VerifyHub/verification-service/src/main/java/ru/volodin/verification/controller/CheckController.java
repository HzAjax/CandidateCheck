package ru.volodin.verification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volodin.verification.model.CheckRequest;
import ru.volodin.verification.model.CheckResponse;
import ru.volodin.verification.service.CheckService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService service;

    @PostMapping("/checks")
    public CheckResponse send(@Valid @RequestBody CheckRequest req) {
        final String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            log.info("Incoming request: requestId={}, typeCheck={}", req.requestId(), req.typeCheck());
            service.process(req);
            log.info("Outgoing response: status=SENT, typeCheck={}", req.typeCheck());
            return new CheckResponse("SENT", req.typeCheck(), traceId);
        } finally {
            MDC.clear();
        }
    }
}
