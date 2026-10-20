package com.skillovilla.readingmaterial.dto.request;

import com.skillovilla.readingmaterial.entity.MaterialType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaterialRequest(
    @NotBlank(message = "Title is required")
    String title,
    
    @NotNull(message = "Material type is required")
    MaterialType materialType,
    
    @Valid
    PdfDetailsRequest pdfDetails,
    
    @Valid
    ArticleDetailsRequest articleDetails,
    
    @Valid
    BookDetailsRequest bookDetails,
    
    @Valid
    SlideDeckDetailsRequest slideDeckDetails
) {}
