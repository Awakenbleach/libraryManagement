package com.skillovilla.readingmaterial.dto.response;

import java.util.UUID;

public record SeriesPartResponse(
    int partOrder,
    UUID materialId,
    String materialTitle,
    boolean completed
) {}
