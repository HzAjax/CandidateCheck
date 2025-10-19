package ru.volodin.loader.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.volodin.loader.model.DriverLicense;
import ru.volodin.loader.model.Inn;
import ru.volodin.loader.model.Passport;
import ru.volodin.loader.model.Person;
import ru.volodin.loader.model.TypeCheck;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VerifyHubClient {

    private static final String SERIES = "series";
    private static final String NUMBER = "number";
    private static final String DATE_ISSUE = "dateIssue";
    private static final String INN = "inn";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final ObjectMapper om;
    private final URI endpoint;

    /**
     * @param gatewayBaseUrl базовый URL шлюза (например, http://host:port/api)
     */
    public VerifyHubClient(String gatewayBaseUrl) {
        this.endpoint = URI.create(
                (gatewayBaseUrl.endsWith("/") ? gatewayBaseUrl : gatewayBaseUrl + "/") + "api/v1/checks"
        );
        this.om = new ObjectMapper();
        this.om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Формирует JSON из Person + requestId и отправляет POST на /api/v1/checks
     * Ретраит только при сетевых ошибках и 5xx. На 4xx — не ретраит.
     *
     * @param p         данные человека
     * @param requestId корреляционный бизнес-ID отправки
     */
    public void send(Person p, String requestId) throws Exception {
        final Map<String, Object> person = new HashMap<>();
        person.put("firstName", p.getFirstName());
        person.put("lastName",  p.getLastName());

        if (p.getPassport() != null) {
            final Map<String, Object> pass = new HashMap<>();
            pass.put(SERIES,  p.getPassport().getSeries());
            pass.put(NUMBER,  p.getPassport().getNumber());
            pass.put(DATE_ISSUE, p.getPassport().getDateIssue() != null ? p.getPassport().getDateIssue().toString() : null);
            person.put("passport", pass);
        }
        if (p.getInn() != null) {
            final Map<String, Object> inn = new HashMap<>();
            inn.put(SERIES,  p.getInn().getSeries());
            inn.put(NUMBER,  p.getInn().getNumber());
            inn.put(INN,     p.getInn().getInn());
            person.put(INN, inn);
        }
        if (p.getDriversLicense() != null) {
            final Map<String, Object> dl = new HashMap<>();
            dl.put(SERIES,    p.getDriversLicense().getSeries());
            dl.put(NUMBER,    p.getDriversLicense().getNumber());
            dl.put(DATE_ISSUE, p.getDriversLicense().getDateIssue() != null ? p.getDriversLicense().getDateIssue().toString() : null);
            person.put("driversLicense", dl);
        }

        final Map<String, Object> body = new HashMap<>();
        body.put("requestId", requestId);
        body.put("typeCheck", p.getTypeCheck().name());
        body.put("person",    person);

        final String json = om.writeValueAsString(body);

        final HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        int attempts = 0;
        final int maxAttempts = 3;
        long wait = 250;

        while (true) {
            attempts++;
            try {
                final HttpResponse<Void> response =
                        http.send(request, HttpResponse.BodyHandlers.discarding());
                final int sc = response.statusCode();

                if (sc >= 200 && sc < 300) {
                    return;
                }

                if (sc >= 500 && sc < 600 && attempts < maxAttempts) {
                    Thread.sleep(waitWithJitter(wait));
                    wait = (long) (wait * 1.7);
                    continue;
                }

                if (sc >= 400 && sc < 500) {
                    throw new IllegalStateException("Client error " + sc + " (no retry)");
                }

                throw new RuntimeException("Unexpected status " + sc);

            } catch (java.io.IOException | InterruptedException e) {
                if (attempts < maxAttempts) {
                    Thread.sleep(waitWithJitter(wait));
                    wait = (long) (wait * 1.7);
                    continue;
                }
                throw e;
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    /**
     * Джиттер к базовой задержке, чтобы разнести повторы.
     */
    private long waitWithJitter(long base) {
        long jitter = ThreadLocalRandom.current().nextLong((long) (base * 0.25));
        return base + jitter;
    }

    /**
     * Быстрая предвалидация required-полей перед отправкой.
     * Бросает IllegalArgumentException при отсутствии обязательных данных.
     */
    public static void validate(Person p) {
        Objects.requireNonNull(p, "Person is null");

        final TypeCheck type = Objects.requireNonNull(p.getTypeCheck(), "typeCheck is required");

        switch (type) {
            case PASSPORT -> requirePassport(p.getPassport());
            case INN -> requireInn(p.getInn());
            case DRIVERS_LICENSE -> requireDriversLicense(p.getDriversLicense());
        }
    }

    private static void requirePassport(Passport passport) {
        if (passport == null) throw new IllegalArgumentException("passport object is required");
        if (isBlank(passport.getSeries())) throw new IllegalArgumentException("passport.series empty");
        if (isBlank(passport.getNumber())) throw new IllegalArgumentException("passport.number empty");
        if (passport.getDateIssue() == null) throw new IllegalArgumentException("passport.dateIssue required");
    }

    private static void requireInn(Inn inn) {
        if (inn == null) throw new IllegalArgumentException("inn object is required");
        if (isBlank(inn.getInn())) throw new IllegalArgumentException("inn.inn empty");
        if (isBlank(inn.getSeries())) throw new IllegalArgumentException("inn.series empty");
        if (isBlank(inn.getNumber())) throw new IllegalArgumentException("inn.number empty");
    }

    private static void requireDriversLicense(DriverLicense dl) {
        if (dl == null) throw new IllegalArgumentException("driversLicense object is required");
        if (isBlank(dl.getSeries())) throw new IllegalArgumentException("driversLicense.series empty");
        if (isBlank(dl.getNumber())) throw new IllegalArgumentException("driversLicense.number empty");
        if (dl.getDateIssue() == null) throw new IllegalArgumentException("driversLicense.dateIssue required");
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}
