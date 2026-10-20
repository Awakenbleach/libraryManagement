package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateProgressRequest(
    @NotNull(message = "Position value is required")
    @Min(value = 0, message = "Position value must be non-negative")
    Integer positionValue,
    
    @NotNull(message = "Expected revision is required")
    Long expectedRevision,
    
    UUID materialVersionId
) {
    // materialVersionId is optional — if null, uses the current LIVE version
}
