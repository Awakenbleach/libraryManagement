package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSeriesRequest(
    @NotBlank(message = "Series title is required")
    String title
) {}
