package com.skillovilla.readingmaterial.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeriesResponse(
    UUID id,
    String title,
    Instant createdAt,
    List<SeriesPartResponse> parts,
    int totalParts,
    int completedParts,
    boolean finished
) {}
