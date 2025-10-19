В репозитории два приложения:

## 1) VerifyHub (микросервисы, Spring Cloud)

* **service-discovery** — Eureka-сервер (реестр сервисов).
* **api-gateway** — единая точка входа, проксирует `/api/v1/**` на verification-service.
* **verification-service** — принимает `POST /v1/checks` и отправляет запросы во внешний сервис (эмулируется WireMock); в логах фиксирует `passport was sent / inn was sent / driver's license was sent`.
* **wiremock** — заглушки внешних проверок.

## 2) CandidateLoader (Servlet, без Spring)

* Принимает base64-файл с массивом `Person[]`, многопоточно отправляет проверки в VerifyHub через `/api/v1/checks`, возвращает краткую сводку обработки.