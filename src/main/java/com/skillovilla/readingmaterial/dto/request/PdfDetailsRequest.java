package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PdfDetailsRequest(
    @NotNull(message = "Page count is required")
    @Min(value = 1, message = "Page count must be greater than zero")
    Integer pageCount,
    
    @NotBlank(message = "File URL is required")
    String fileUrl
) {}
