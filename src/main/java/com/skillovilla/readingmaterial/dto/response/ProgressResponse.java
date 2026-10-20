package com.skillovilla.readingmaterial.dto.response;

import com.skillovilla.readingmaterial.entity.PositionType;

import java.time.Instant;
import java.util.UUID;

public record ProgressResponse(
    UUID id,
    UUID materialVersionId,
    int versionNumber,
    int positionValue,
    PositionType positionType,
    boolean completed,
    long progressRevision,
    Instant updatedAt
) {}
