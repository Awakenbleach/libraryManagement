package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddSeriesPartRequest(
    @NotNull(message = "Material ID is required")
    UUID materialId,
    
    @NotNull(message = "Part order is required")
    @Min(value = 1, message = "Part order must be positive")
    Integer partOrder
) {}
