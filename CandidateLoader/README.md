# CandidateLoader

Servlet-приложение (Jakarta Servlet 6), принимает base64-файл с кандидатами (`Person[]`), **многопоточно** отправляет проверки в VerifyHub (через Gateway `/api/v1/checks`) и возвращает сводку обработки. Каждый объект обрабатывается **ровно один раз**.

## Быстрый старт (Tomcat или Docker)

### Вариант A — Tomcat

1. Сборка

```
.\mvnw.cmd clean package -DskipTests
```

2. Деплой в Tomcat (Servlet 6 / Jakarta EE 10). Параметры:

* `BASE_URL` — базовый адрес Gateway VerifyHub (в `web.xml`, ENV или `-DBASE_URL=...`, по умолчанию `http://localhost:8080`).
* `THREADS` — число потоков-воркеров (ENV; по умолчанию `min(CPU, 8)`).

### Вариант B — Docker (compose)

```yaml
services:
  loader:
    build: .
    container_name: loader
    environment:
      BASE_URL: http://gateway:8080
    ports:
      - "8085:8080"
    networks: [verifyhub-net]
    restart: unless-stopped

networks:
  verifyhub-net:
    external: true
```

> Внешняя сеть должна существовать заранее:

```
docker network create verifyhub-net
```

Запуск:

```
# (в корне модуля CandidateLoader)
docker compose up --build -d
```

## HTTP API

### `POST /uploader`

* Docker: `http://localhost:8085/uploader`
* Tomcat (WAR): `http://<host>/<context>/uploader` (например, `/CandidateLoader/uploader`)

Тело:

```json
{ "payloadBase64": "<base64 от JSON массива Person[]>" }
```

Ответ (успех):

```json
{ "accepted": 10, "processed": 10, "errors": 0, "remaining": 0 }
```

Пример:

```
BASE64=$(base64 -w0 persons.json)   # persons.json — массив Person[]
curl -s http://localhost:8085/uploader \
  -H "Content-Type: application/json" \
  -d "{\"payloadBase64\":\"$BASE64\"}"
```

## Формат данных

`Person`:

```json
{
  "firstName": "Ivan",
  "lastName": "Ivanov",
  "typeCheck": "PASSPORT | INN | DRIVERS_LICENSE",
  "passport": { "series": "1234", "number": "567890", "dateIssue": "2020-01-01" },
  "inn": { "series": "1234", "number": "123456789", "inn": "123456789012" },
  "driversLicense": { "series": "1111", "number": "222222", "dateIssue": "2019-05-05" }
}
```

> У одного кандидата — **ровно один** `typeCheck` и соответствующий блок.

## Пример запроса к CandidateLoader

POST http://localhost:8085/uploader  
Content-Type: application/json

```
{
  "payloadBase64": "W3siZmlyc3ROYW1lIjoi0JjQvNC10YDQvdC40Y8iLCJsYXN0TmFtZSI6ItCa0YPRgNC10YLQsCIsInR5cGVDaGVjayI6IlBBU1NQT1JUIiwicGFzc3BvcnQiOnsic2VyaWVzIjoiMTIzNCIsIm51bWJlciI6IjU2Mjc4OTAiLCJkYXRlSXNzdWUiOiIyMDIwLTAxLTE1In19LHsiZmlyc3ROYW1lIjoi0JPQsNGC0YDQvtC00LXQvdGPIiwibGFzdE5hbWUiOiLQkNGA0LDQvNC10LIiLCJ0eXBlQ2hlY2siOiJJTk4iLCJpbm4iOnsic2VyaWVzIjoiNzEyNyIsIm51bWJlciI6Ijg4ODIzNDk5OSIsImlubiI6IjUwMjkxMTIyMzM0NCJ9fSx7ImZpcnN0TmFtZSI6ItCf0L7QvNC40Y8iLCJsYXN0TmFtZSI6ItCU0L7Qv9GA0L7Qu9C+0LIiLCJ0eXBlQ2hlY2siOiJEUklWRVJTX0xJQ0VOU0UiLCJkcml2ZXJzTGljZW5zZSI6eyJzZXJpZXMiOiIxMjM0IiwibnVtYmVyIjoiMTIzNDU2IiwiZGF0ZUlzc3VlIjoiMjAyMS0wMi0xMCJ9fV0="
}
```

### Пример ответа от CandidateLoader

```
{
  "accepted": 3,
  "processed": 2,
  "errors": 1,
  "remaining": 0
}
```