package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddMaterialToCollectionRequest(
    @NotNull(message = "Material ID is required")
    UUID materialId
) {}
