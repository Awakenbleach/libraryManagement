package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlideDeckDetailsRequest(
    @NotNull(message = "Slide count is required")
    @Min(value = 1, message = "Slide count must be greater than zero")
    Integer slideCount,
    
    @NotBlank(message = "File URL is required")
    String fileUrl
) {}
