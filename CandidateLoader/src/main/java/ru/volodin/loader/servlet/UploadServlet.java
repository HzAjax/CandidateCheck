package ru.volodin.loader.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.volodin.loader.model.Person;
import ru.volodin.loader.model.Result;
import ru.volodin.loader.model.dto.UploadDto;
import ru.volodin.loader.service.ProcessingService;
import ru.volodin.loader.service.VerifyHubClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = "/uploader")
public class UploadServlet extends HttpServlet {

    private static final String BASE_URL = "BASE_URL";

    private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);

    private transient ObjectMapper om;
    private transient VerifyHubClient client;
    private transient int threads;

    /**
     * Инициализация сервлета:
     * - настраиваем ObjectMapper,
     * - читаем BASE_URL из system/env/context параметров,
     * - создаём HTTP-клиент,
     * - определяем количество потоков для обработки.
     */
    @Override
    public void init() {
        this.om = new ObjectMapper().findAndRegisterModules();

        String gw =
                Optional.ofNullable(System.getProperty(BASE_URL))
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> Optional.ofNullable(System.getenv(BASE_URL))
                                .filter(s -> !s.isBlank())
                                .orElseGet(() -> Optional.ofNullable(getServletContext().getInitParameter(BASE_URL))
                                        .filter(s -> !s.isBlank())
                                        .orElse("http://localhost:8080")));

        this.client = new VerifyHubClient(gw);
        this.threads = Integer.parseInt(
                System.getenv().getOrDefault("THREADS",
                        String.valueOf(Math.min(Runtime.getRuntime().availableProcessors(), 8))));
        log.info("UploadServlet initialized: BASE_URL={}, threads={}", gw, threads);
    }

    /**
     * Приём батча персон в виде JSON:
     * 1) читаем UploadDto из тела запроса,
     * 2) декодируем payloadBase64 в массив Person,
     * 3) параллельно обрабатываем через ProcessingService,
     * 4) возвращаем краткую сводку (accepted/processed/errors/remaining).
     *
     * При ошибке — 400 и текст ошибки.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            final UploadDto dto = om.readValue(req.getInputStream(), UploadDto.class);

            final byte[] bytes = Base64.getDecoder().decode(dto.getPayloadBase64());
            final List<Person> persons = Arrays.asList(om.readValue(bytes, Person[].class));

            final ProcessingService svc = new ProcessingService(threads, client);
            final Result r = svc.process(persons);

            final String out = """
                {"accepted":%d,"processed":%d,"errors":%d,"remaining":%d}
                """.formatted(r.getAccepted(), r.getProcessed(), r.getErrors(), r.getRemaining());

            resp.getWriter().write(out);
        } catch (Exception e) {
            log.error("Upload processing failed", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
