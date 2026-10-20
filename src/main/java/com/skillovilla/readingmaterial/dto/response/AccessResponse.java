package com.skillovilla.readingmaterial.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccessResponse(
    UUID id,
    String accessType,
    UUID targetId,
    UUID readerId,
    Instant grantedAt,
    LocalDate accessEndDate
) {}

