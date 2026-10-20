package com.skillovilla.readingmaterial.dto.response;

import com.skillovilla.readingmaterial.entity.MaterialType;

import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
    UUID id,
    String title,
    MaterialType materialType,
    Instant createdAt,
    Instant updatedAt,
    MaterialVersionResponse liveVersion
) {}
