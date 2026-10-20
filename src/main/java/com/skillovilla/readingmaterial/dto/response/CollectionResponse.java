package com.skillovilla.readingmaterial.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CollectionResponse(
    UUID id,
    String name,
    Instant createdAt
) {}
