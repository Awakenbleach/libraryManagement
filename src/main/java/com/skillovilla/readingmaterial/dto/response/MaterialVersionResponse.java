package com.skillovilla.readingmaterial.dto.response;

import com.skillovilla.readingmaterial.entity.VersionStatus;

import java.time.Instant;
import java.util.UUID;

public record MaterialVersionResponse(
    UUID id,
    int versionNumber,
    VersionStatus status,
    UUID createdBy,
    Instant createdAt,
    Instant publishedAt,
    Object typeDetails
) {}
