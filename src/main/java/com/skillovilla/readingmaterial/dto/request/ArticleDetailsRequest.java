package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ArticleDetailsRequest(
    @NotNull(message = "Word count is required")
    @Min(value = 1, message = "Word count must be greater than zero")
    Integer wordCount
) {}
