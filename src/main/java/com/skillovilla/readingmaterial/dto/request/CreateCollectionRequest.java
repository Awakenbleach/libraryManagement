package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCollectionRequest(
    @NotBlank(message = "Collection name is required")
    String name
) {}
