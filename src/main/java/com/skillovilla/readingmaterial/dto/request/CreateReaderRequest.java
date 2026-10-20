package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateReaderRequest(
    @NotBlank(message = "Reader name is required")
    String name
) {}
