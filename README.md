# Restful Template (Spring Boot + JPA + PostgreSQL)

This project is a RESTful template built on Spring Boot 4, JPA, PostgreSQL, HATEOAS, JSON Patch, and RFC 9457 Problem Details.
It uses Jackson 3 (`tools.jackson.*`) and supports field shaping, pagination, ordering, and method-aware links.

**Features**
1. RFC 9457 Problem Details for errors
2. RFC 8288-style `Link` headers with `method`
3. HATEOAS `_links` in response bodies
4. JSON Patch (`application/json-patch+json`) via `zjsonpatch`
5. Pagination with `X-Pagination` JSON header
6. Ordering with `orderBy` and SQL-injection safe field allowlist
7. Field shaping via `fields`

**Requirements**
1. JDK 25
2. PostgreSQL

**Configuration**
Edit `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/restful
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

**Run**
```bash
./gradlew bootRun
```

**API Overview**
Base path: `/users`

1. `GET /users`
2. `GET /users/{id}`
3. `POST /users`
4. `PATCH /users/{id}` (`application/json-patch+json`)
5. `DELETE /users/{id}`

**Pagination**
Query params:
1. `page`
2. `pageSize`

Response header:
```http
X-Pagination: {"page":1,"pageSize":10,"total":120,"totalPage":12}
```

`Link` header includes `self`, `first`, `prev`, `next`, `last` with `method="GET"`.

**Ordering**
Query param:
```
orderBy=field ASC,field2 DESC
```
Delimiter is hard-coded in controller as `,`.

Only allowed fields are accepted; others are ignored for safety.

**Field Shaping**
Query param:
```
fields=id,username,_links
```

Behavior:
1. Only `Shapable` response models are filtered.
2. For collections or maps, only `Shapable` values are shaped.
3. If `_links` is requested, `links` is mapped to `_links`.

**HATEOAS Links**
Each returned user includes `_links` with method-aware actions:
1. `self` `GET`
2. `update` `PUT` (URI provided, no controller method yet)
3. `patch` `PATCH`
4. `delete` `DELETE`

**JSON Patch**
Endpoint:
```
PATCH /users/{id}
Content-Type: application/json-patch+json
```

Example:
```bash
curl -X PATCH 'http://localhost:8080/users/1' \
  -H 'Content-Type: application/json-patch+json' \
  -d '[{"op":"replace","path":"/username","value":"alice"}]'
```

Only `username` and `email` are allowed in patch operations.

**Error Format (RFC 9457)**
Errors are returned as Problem Details:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid JSON Patch",
  "instance": "/users/1"
}
```
