# Design Document — Reading Material Management System

## 1. Database Tables: How PDF page count and book chapters coexist

Each material type uses a **dedicated detail table** with `version_id` as both PK and FK to `material_version` (JPA `@MapsId` pattern). No nullable shared columns, no JSON, no EAV.

- `pdf_version_details(version_id, page_count, file_url)`  
- `article_version_details(version_id, word_count)`  
- `book_version_details(version_id, author)` + `book_chapter(id, version_id, chapter_number, title)`  
- `slide_deck_version_details(version_id, slide_count, file_url)`  

`material_type` lives on `material` (never changes). Every `material_version` has exactly one matching detail row.

## 2. Fifth Material Type (VIDEO)

Adding `VIDEO` with `duration_minutes` requires **only**:

| Add | No change |
|-----|----------|
| `V8__create_video_detail_table.sql` | `material`, `material_version` |
| `MaterialType.VIDEO` enum value | All existing detail tables |
| `VideoVersionDetails` entity | All existing rows |
| `VideoVersionDetailsRepository` | — |
| `VideoDetailService implements TypeDetailService` | `TypeDetailServiceFactory` (auto-discovers Spring beans) |

`ALTER TABLE material ADD ...` is **not required**.

## 3. Single LIVE Version Enforcement

```sql
CREATE UNIQUE INDEX idx_one_live_version
  ON material_version(material_id) WHERE status = 'LIVE';
```

This PostgreSQL partial unique index makes two concurrent LIVE rows for the same material physically impossible — the DB raises a unique constraint violation regardless of application-level checks. `GlobalExceptionHandler` catches `DataIntegrityViolationException`, inspects the constraint name, and returns **HTTP 409** with code `PUBLISH_CONFLICT`. `PublishService` also acquires a `PESSIMISTIC_WRITE` lock on the material row to serialize publish operations before the index is tested.

## 4. Reader Position Storage

One `reading_progress` table with `position_value` (INTEGER) and `position_type` (enum):

| Type | position_type | Semantics | Finished when |
|------|--------------|-----------|---------------|
| PDF | PAGE | current page (1…page_count) | == page_count |
| Article | PERCENTAGE | 0…100 | >= 100 |
| Book | CHAPTER | highest completed chapter | == total_chapters |
| Slide Deck | SLIDE | current slide (1…slide_count) | == slide_count |

Validation is type-aware in each `TypeDetailService.validatePosition()` implementation.

## 5. Day-14 / Day-16: Version Transition and Rollback

**Policy**: Progress is permanently linked to the `material_version_id` the reader read.

- Opening material → always returns the LIVE version.
- If the reader has progress on an **older version**, `ResumeResponse` includes `progressOnDifferentVersion=true` plus the old `ProgressResponse`. The client may prompt the user.
- **Rollback** (re-publishing V1 after V3 was LIVE): `PublishService` sets current LIVE → ARCHIVED, sets V1 → LIVE. All versions are retained. The reader's V1 progress is immediately valid again with no data migration.

## 6. Out-of-Order Progress Updates

`reading_progress.progress_revision` (BIGINT, starts at 1). Every update requires `expectedRevision`:

```sql
UPDATE reading_progress
SET position_value = ?, progress_revision = progress_revision + 1
WHERE id = ? AND progress_revision = ?
```

If 0 rows updated → **HTTP 409 Conflict**. A late-arriving laptop request (revision 1) after the phone advanced to revision 2 fails with 409. The client reloads current state before retrying. Brand-new progress records always start at revision 1.

## 7. Access Enforcement

Centralized in `ReaderAccessService`. Access is valid when:

```
(direct reader_material_access, not expired)
OR (reader_collection_access for a collection containing the material, not expired)
```

Expiry: `access_end_date IS NULL OR access_end_date >= CURRENT_DATE` (today is inclusive).

Enforced at **every** reader-facing operation via SQL subqueries in `MaterialRepository` — not post-load Java filtering. Counts use the same query. Readers cannot access materials by guessing IDs.

## 8. Series Progress

Never stored — always derived at query time:

1. Load `series_part` rows ordered by `part_order`
2. For each part, find the LIVE `material_version`
3. Check `reading_progress(reader_id, live_version_id).completed`
4. Return `completedParts / totalParts`; `finished = (completedParts == totalParts && totalParts > 0)`

A material may appear in multiple series; the same progress row contributes to all. Queries are batched to avoid N+1.

## 9. Scale: 50k Materials, 100k Readers, 20M Positions

Current: proper indexes, SQL-level access filtering, optimistic concurrency, offset pagination, HikariCP. For production: keyset pagination, read replicas, Redis for access TTL cache, partitioned `reading_progress`, async progress ingestion, materialized series views.

## 10. Known Limitations

- No authentication — header-based reader identity is spoofable
- Type-specific field search (minPageCount) needs dedicated per-type SQL queries (wired but not fully implemented)
- Book chapter completion is not tracked individually — only highest chapter reached
- Seed data (V6) runs on every startup; needs a `dev`-profile guard in production
