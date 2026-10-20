# Reading Material Management System

A backend API implementing a reading material management system with material versioning, access control, reader progress tracking, series, and full PostgreSQL enforcement of business rules.

**Assignment**: SkilloVilla Backend Java/Spring Boot take-home

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Database | PostgreSQL 17 |
| ORM | Spring Data JPA / Hibernate 6 |
| Migrations | Flyway 10 |
| Build | Maven 3.9 |
| Tests | JUnit 5 + Testcontainers |
| Validation | Jakarta Bean Validation 3.0 |

## Prerequisites

- **Java 21 JDK** installed and set as `JAVA_HOME`
  - IntelliJ IDEA bundled JBR 21 works: `C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.5\jbr`
- **PostgreSQL 17** running locally or via Docker
- **Docker** installed (required for integration tests via Testcontainers)
- **Maven 3.9+** (or use IntelliJ bundled Maven at `C:\Users\ASUS\.m2\wrapper\dists\apache-maven-3.9.9-bin\...`)

## Database Setup

### Option A — Local PostgreSQL
```sql
CREATE DATABASE reading_material;
CREATE USER reading_user WITH PASSWORD 'reading_pass';
GRANT ALL PRIVILEGES ON DATABASE reading_material TO reading_user;
```

### Option B — Docker (Recommended)
```bash
docker run -d --name pg17 \
  -e POSTGRES_DB=reading_material \
  -e POSTGRES_USER=reading_user \
  -e POSTGRES_PASSWORD=reading_pass \
  -p 5432:5432 \
  postgres:17-alpine
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reading_material
spring.datasource.username=reading_user
spring.datasource.password=reading_pass

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

## Running the Application

```bash
# Windows PowerShell — set JAVA_HOME to Java 21
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.5\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Run
& "C:\Users\ASUS\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

Application starts on **http://localhost:8080**

Flyway migrations run automatically on startup and seed initial test data (V6 seed migration).

## Running Tests

> **Requires Docker** — Testcontainers will automatically start a PostgreSQL 17 container.

```bash
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.5\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

& "C:\Users\ASUS\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" test
```

Testcontainers will pull `postgres:17-alpine` automatically on first run.

## Reader Identification

All reader-facing endpoints require the `X-Reader-Id` header:

```
X-Reader-Id: <reader-uuid>
```

Example:
```bash
curl -H "X-Reader-Id: 00000000-0000-0000-0000-000000000001" \
  http://localhost:8080/api/v1/reader/materials
```

Admin endpoints accept an optional `X-Admin-Id` header (not enforced — any UUID).

## SQL Logging

To enable SQL statement logging, add to `application.properties`:

```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

> Keep these OFF in production. The default configuration does not enable SQL logging.

## Seed Test Data (V6 Migration)

Flyway migration `V6__seed_test_data.sql` loads the following on startup:

| Entity | ID | Notes |
|--------|----|-------|
| Reader 1 | `00000000-0000-0000-0000-000000000001` | Has collection access |
| Reader 2 | `00000000-0000-0000-0000-000000000002` | Has direct access to Book |
| PDF Material | `10000000-0000-0000-0000-000000000001` | LIVE — in collection + series |
| Article Material | `10000000-0000-0000-0000-000000000002` | LIVE — in collection + series |
| Book Material | `10000000-0000-0000-0000-000000000003` | LIVE — Reader 2 has direct access |
| Slide Deck | `10000000-0000-0000-0000-000000000004` | DRAFT only |
| Collection | `20000000-0000-0000-0000-000000000001` | Contains PDF + Article |
| Series | `30000000-0000-0000-0000-000000000001` | PDF (part 1) + Article (part 2) |

## API Reference

### Admin — Materials

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/materials` | Create material (starts as V1 DRAFT) |
| `GET` | `/api/v1/admin/materials/{materialId}` | Get material + all versions (admin view) |
| `POST` | `/api/v1/admin/materials/{materialId}/versions` | Copy version → new DRAFT |
| `PUT` | `/api/v1/admin/materials/{materialId}/versions/{versionId}` | Update DRAFT version |
| `POST` | `/api/v1/admin/materials/{materialId}/versions/{versionId}/publish` | Publish version |

### Admin — Access Control

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/readers` | Create reader |
| `POST` | `/api/v1/admin/readers/{readerId}/materials/{materialId}/access` | Grant direct material access |
| `DELETE` | `/api/v1/admin/readers/{readerId}/materials/{materialId}/access` | Revoke direct material access |
| `POST` | `/api/v1/admin/readers/{readerId}/collections/{collectionId}/access` | Grant collection access |
| `DELETE` | `/api/v1/admin/readers/{readerId}/collections/{collectionId}/access` | Revoke collection access |

### Admin — Collections

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/collections` | Create collection |
| `POST` | `/api/v1/admin/collections/{collectionId}/materials` | Add material to collection |
| `DELETE` | `/api/v1/admin/collections/{collectionId}/materials/{materialId}` | Remove material from collection |

### Admin — Series

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/series` | Create series |
| `POST` | `/api/v1/admin/series/{seriesId}/parts` | Add material as series part |

### Reader — Materials (require `X-Reader-Id` header)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/reader/materials` | List accessible materials (paginated) |
| `GET` | `/api/v1/reader/materials/search` | Search with type/title filters |
| `GET` | `/api/v1/reader/materials/{materialId}` | Open material (LIVE version) |
| `GET` | `/api/v1/reader/materials/{materialId}/resume` | Resume — LIVE version + progress |
| `PUT` | `/api/v1/reader/materials/{materialId}/progress` | Update progress |

### Reader — Series

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/reader/series/{seriesId}` | Series with derived progress |

## Example Requests & Responses

### Create PDF Material
```bash
curl -X POST http://localhost:8080/api/v1/admin/materials \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Introduction to Java",
    "materialType": "PDF",
    "pdfDetails": { "pageCount": 250, "fileUrl": "http://example.com/java.pdf" }
  }'
```

**Response (201):**
```json
{
  "id": "a1b2c3d4-...",
  "title": "Introduction to Java",
  "materialType": "PDF",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z",
  "versions": [{
    "id": "v1-uuid-...",
    "versionNumber": 1,
    "status": "DRAFT",
    "createdBy": null,
    "createdAt": "2024-01-15T10:00:00Z",
    "publishedAt": null,
    "typeDetails": { "pageCount": 250, "fileUrl": "http://example.com/java.pdf" }
  }]
}
```

### Publish Version
```bash
curl -X POST http://localhost:8080/api/v1/admin/materials/{materialId}/versions/{versionId}/publish
```

### Update Progress (first time)
```bash
curl -X PUT http://localhost:8080/api/v1/reader/materials/{materialId}/progress \
  -H 'X-Reader-Id: {readerId}' \
  -H 'Content-Type: application/json' \
  -d '{ "positionValue": 5, "expectedRevision": null }'
```

**Response (200):**
```json
{
  "id": "...",
  "materialVersionId": "...",
  "versionNumber": 1,
  "positionValue": 5,
  "positionType": "PAGE",
  "completed": false,
  "progressRevision": 1,
  "updatedAt": "2024-01-15T10:05:00Z"
}
```

### Stale Progress Update — 409 Conflict
```bash
curl -X PUT http://localhost:8080/api/v1/reader/materials/{materialId}/progress \
  -H 'X-Reader-Id: {readerId}' \
  -H 'Content-Type: application/json' \
  -d '{ "positionValue": 3, "expectedRevision": 1 }'  # but current revision is 2
```

**Response (409):**
```json
{
  "timestamp": "2024-01-15T10:06:00Z",
  "status": 409,
  "code": "CONFLICT",
  "message": "Progress update conflict. Expected revision 1 but current revision is 2",
  "path": "/api/v1/reader/materials/.../progress"
}
```

### Concurrent Publish — 409 Conflict
If two admins publish simultaneously and both versions are LIVE, PostgreSQL's partial unique index (`idx_one_live_version`) prevents it:

**Response (409):**
```json
{
  "timestamp": "...",
  "status": 409,
  "code": "PUBLISH_CONFLICT",
  "message": "Another version is already LIVE for this material. Only one LIVE version is allowed.",
  "path": "/api/v1/admin/materials/.../versions/.../publish"
}
```

## Architecture Overview

```
HTTP Request
     ↓
 Controller  (thin — validates input, delegates to service)
     ↓
 Service     (business rules, @Transactional boundaries)
     ↓
 Repository  (Spring Data JPA — SQL executed by PostgreSQL)
     ↓
 PostgreSQL 17  (Flyway schema, partial unique index, FK constraints)
```

### Package Structure

```
com.skillovilla.readingmaterial
├── controller/          REST controllers
├── service/             Business logic
├── repository/          Spring Data JPA interfaces
├── entity/              JPA entities + enums
├── dto/
│   ├── request/         Request records (Bean Validation)
│   └── response/        Response records
└── exception/           Custom exceptions + GlobalExceptionHandler
```

## Design Decisions

See [DESIGN.md](DESIGN.md) for the full 2-page design document.

Key decisions:
- **Type-specific detail tables** (one-to-one shared PK) — no JSON blobs, no nullable columns
- **Partial unique index** for single-live-version DB enforcement
- **Progress revision** (optimistic concurrency) for out-of-order update prevention
- **Centralized access evaluation** — all reader routes use the same SQL-level filter
- **Version-specific progress** — progress survives version changes, resume shows diff flag

## Known Limitations

1. Reader identity via `X-Reader-Id` header — trivially spoofable (no auth)
2. Search supports type+title filtering only; minPageCount/maxPageCount fields are wired in the controller but require additional type-specific SQL queries to work end-to-end
3. Book progress tracks the highest completed chapter number only — no per-chapter completion status
4. No rate limiting or request throttling
5. Seed data (V6) loads on every startup — in production this would be gated to a `dev` profile

## Production Considerations

For 50k materials / 100k readers / 20M progress records:
- **Keyset pagination** instead of offset (avoids full table scan for deep pages)
- **Read replicas** for reader-facing list/search/resume queries
- **Redis cache** for access decisions (TTL aligned to `access_end_date`)
- **`reading_progress` partitioning** by reader_id hash range
- **Materialized views** for series progress (refresh on progress update)
- **Async progress writes** via an outbox / message queue
- **HikariCP tuning**: `maximumPoolSize = 2 × CPU_COUNT + 1`
