package com.skillovilla.readingmaterial.dto.response;

import com.skillovilla.readingmaterial.entity.MaterialType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MaterialDetailResponse(
    UUID id,
    String title,
    MaterialType materialType,
    Instant createdAt,
    Instant updatedAt,
    List<MaterialVersionResponse> versions
) {}
