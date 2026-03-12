# URL Shortener

REST API сервіс для скорочення URL з JWT-аутентифікацією, PostgreSQL, Flyway, OpenAPI, Docker Compose, Testcontainers та Gradle.

## Swagger / OpenAPI

Після запуску:
- `/swagger-ui.html`
- `/v3/api-docs`

## Змінні оточення

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `APP_PUBLIC_BASE_URL`

### Приклад

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=url_shortener
export DB_USERNAME=url_user
export DB_PASSWORD=url_password
export JWT_SECRET=ChangeMeToASuperLongSecretKeyForJwtAtLeast32Chars
export JWT_EXPIRATION_MINUTES=120
export APP_PUBLIC_BASE_URL=http://localhost:8080
```

## Запуск БД через Docker Compose

```bash
docker compose up -d
```

## Тести

```bash
gradle clean test jacocoTestReport jacocoTestCoverageVerification
```

## Основні endpoints

### Auth
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Links
- `POST /api/v1/links`
- `GET /api/v1/links`
- `GET /api/v1/links?activeOnly=true`
- `GET /api/v1/links/{id}`
- `GET /api/v1/links/{id}/stats`
- `PUT /api/v1/links/{id}/expiration`
- `DELETE /api/v1/links/{id}`

### Redirect
- `GET /api/v1/redirect/{code}`
