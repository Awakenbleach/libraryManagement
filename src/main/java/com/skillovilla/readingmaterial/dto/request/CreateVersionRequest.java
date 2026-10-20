package com.skillovilla.readingmaterial.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVersionRequest(
    @NotNull(message = "Source version ID is required")
    UUID sourceVersionId,
    
    @Valid
    PdfDetailsRequest pdfDetails,
    
    @Valid
    ArticleDetailsRequest articleDetails,
    
    @Valid
    BookDetailsRequest bookDetails,
    
    @Valid
    SlideDeckDetailsRequest slideDeckDetails
) {}
