# VerifyHub

Микросервисное решение на Spring Cloud для проверки кандидата по трём типам данных:

* паспорт
* ИНН
* водительское удостоверение (ВУ)

Компоненты: **Eureka (service-discovery)**, **Spring Cloud Gateway (api-gateway)**, **verification-service**, **WireMock** (заглушка внешних сервисов).

## Цель и требования

**Задача:** Реализовать API для приёма заявки на проверку и отправки запроса во внешний сервис (эмулируется WireMock).

## Структура

```
api-gateway/            # /api/v1/** → lb://verification-service (stripPrefix /api)
service-discovery/      # Eureka (:8761)
verification-service/   # POST /v1/checks → вызов WireMock
wiremock/               # mappings: /ext/passport-check|inn-check|dl-check
docker-compose.yml
```

## Быстрый запуск

1. Сборка

```
.\mvnw.cmd clean package -DskipTests
```

2. Сеть Docker (однократно)

```
docker network create verifyhub-net
```

3. Старт стенда

```
docker compose up --build -d
```

4. Проверка

```
curl -s http://localhost:8080/api/v1/checks \
  -H "Content-Type: application/json" \
  -d '{
        "requestId":"123",
        "typeCheck":"PASSPORT",
        "person":{
          "firstName":"Ivan","lastName":"Ivanov",
          "passport":{"series":"1234","number":"567890","dateIssue":"2020-01-01"}
        }
      }'
# ожидаемо: { "status":"SENT", "typeCheck":"PASSPORT", "traceId":"..." }
# в логах verification-service: "passport was sent"
```

## Конфигурация по умолчанию (compose)

* **Gateway:** `http://localhost:8080` → роут на `lb://verification-service`
* **Eureka:** `http://discovery:8761/eureka/` (внутренний адрес)
* **Verification Service:** слушает `:8082`, ходит на `WireMock:9090`
* **WireMock:** маппинги из `./wiremock/mappings`

## Контракты API (кратко)

* `POST /api/v1/checks` (через шлюз) → `verification-service` `/v1/checks`

    * `typeCheck`: `PASSPORT | INN | DRIVERS_LICENSE`
    * Ответ: `{ "status":"SENT", "typeCheck":"...", "traceId":"..." }`
    * Ошибки: `400` (валидация), `500` (внутренняя)
